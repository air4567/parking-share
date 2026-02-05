package com.parking.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parking.entity.Community;
import com.parking.entity.ParkingSpot;
import com.parking.entity.User;
import com.parking.mapper.CommunityMapper;
import com.parking.mapper.ParkingSpotMapper;
import com.parking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingSpotService {

    private final ParkingSpotMapper parkingSpotMapper;
    private final CommunityMapper communityMapper;
    private final UserMapper userMapper;
    private final OrderService orderService;

    private static final Map<String, String> STATUS_TEXT = new HashMap<String, String>() {{
        put("available", "可用");
        put("occupied", "已占用");
        put("reserved", "已预订");
    }};

    /**
     * 推荐车位。当 startTime/endTime 不为空时，仅返回在该时段内无「进行中」订单的车位。
     */
    public List<Map<String, Object>> recommended(Long communityId, BigDecimal lat, BigDecimal lng, BigDecimal maxDistance,
                                                String startTimeStr, String endTimeStr) {
        LambdaQueryWrapper<ParkingSpot> q = new LambdaQueryWrapper<>();
        if (startTimeStr == null || startTimeStr.trim().isEmpty() || endTimeStr == null || endTimeStr.trim().isEmpty()) {
            q.eq(ParkingSpot::getStatus, "available");
        } else {
            q.in(ParkingSpot::getStatus, Arrays.asList("available", "reserved"));
        }
        if (communityId != null) q.eq(ParkingSpot::getCommunityId, communityId);
        q.orderByAsc(ParkingSpot::getPricePerHour);
        q.last("LIMIT 50");
        List<ParkingSpot> list = parkingSpotMapper.selectList(q);

        if (startTimeStr != null && !startTimeStr.trim().isEmpty() && endTimeStr != null && !endTimeStr.trim().isEmpty()) {
            try {
                LocalDateTime start = LocalDateTime.parse(startTimeStr.trim().replace(" ", "T"));
                LocalDateTime end = LocalDateTime.parse(endTimeStr.trim().replace(" ", "T"));
                List<Long> occupiedSpotIds = orderService.findSpotIdsWithOverlappingOngoingOrder(start, end);
                if (occupiedSpotIds != null && !occupiedSpotIds.isEmpty()) {
                    list = list.stream().filter(s -> !occupiedSpotIds.contains(s.getId())).collect(Collectors.toList());
                }
            } catch (Exception ignored) { }
        }

        return toDtoList(list, lat, lng, maxDistance);
    }

    public Map<String, Object> list(int page, int pageSize, String keyword, Long communityId, String sortType) {
        Page<ParkingSpot> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<ParkingSpot> q = new LambdaQueryWrapper<>();
        q.eq(ParkingSpot::getStatus, "available");
        if (communityId != null) q.eq(ParkingSpot::getCommunityId, communityId);
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Long> cids = communityMapper.selectList(
                    new LambdaQueryWrapper<Community>().like(Community::getName, keyword)
            ).stream().map(Community::getId).collect(Collectors.toList());
            if (!cids.isEmpty()) q.in(ParkingSpot::getCommunityId, cids);
            else q.apply("1=0");
        }
        if ("price".equals(sortType)) q.orderByAsc(ParkingSpot::getPricePerHour);
        else if ("distance".equals(sortType)) q.orderByAsc(ParkingSpot::getId);
        else q.orderByDesc(ParkingSpot::getCreateTime);

        Page<ParkingSpot> result = parkingSpotMapper.selectPage(p, q);
        List<Map<String, Object>> list = toDtoList(result.getRecords(), null, null, null);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("hasMore", result.hasNext());
        return data;
    }

    public Map<String, Object> getById(Long id, BigDecimal lat, BigDecimal lng) {
        ParkingSpot s = parkingSpotMapper.selectById(id);
        if (s == null) return null;
        double distance = 0;
        Community c = communityMapper.selectById(s.getCommunityId());
        if (c != null && lat != null && lng != null && c.getLatitude() != null && c.getLongitude() != null) {
            distance = calculateDistance(lat.doubleValue(), lng.doubleValue(),
                    c.getLatitude().doubleValue(), c.getLongitude().doubleValue());
        }
        Map<String, Object> dto = toDto(s, lat, lng, distance);
        if (c != null) dto.put("communityName", c.getName());
        User u = userMapper.selectById(s.getOwnerId());
        if (u != null) {
            dto.put("ownerName", u.getNickname());
            dto.put("ownerAvatar", u.getAvatar());
            dto.put("ownerPhone", u.getPhone());
        }
        return dto;
    }

    public Long publish(Long ownerId, Long communityId, String spotNumber, BigDecimal pricePerHour, String description, String images) {
        ParkingSpot s = new ParkingSpot();
        s.setCommunityId(communityId);
        s.setOwnerId(ownerId);
        s.setSpotNumber(spotNumber);
        s.setPricePerHour(pricePerHour);
        s.setDescription(description);
        s.setImages(images);
        s.setStatus("available");
        parkingSpotMapper.insert(s);
        return s.getId();
    }

    /**
     * 业主更新自己的车位信息
     */
    public boolean update(Long id, Long ownerId, Long communityId, String spotNumber, BigDecimal pricePerHour, String description, String images) {
        ParkingSpot s = parkingSpotMapper.selectById(id);
        if (s == null || !s.getOwnerId().equals(ownerId)) return false;
        s.setCommunityId(communityId);
        s.setSpotNumber(spotNumber);
        s.setPricePerHour(pricePerHour);
        s.setDescription(description);
        s.setImages(images != null ? images : s.getImages());
        parkingSpotMapper.updateById(s);
        return true;
    }

    /**
     * 当前用户发布的车位列表（我的车位）
     */
    public Map<String, Object> myList(Long ownerId, int page, int pageSize) {
        Page<ParkingSpot> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<ParkingSpot> q = new LambdaQueryWrapper<>();
        q.eq(ParkingSpot::getOwnerId, ownerId);
        q.orderByDesc(ParkingSpot::getCreateTime);
        Page<ParkingSpot> result = parkingSpotMapper.selectPage(p, q);
        List<Map<String, Object>> list = toDtoList(result.getRecords(), null, null, null);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("hasMore", result.hasNext());
        data.put("total", result.getTotal());
        return data;
    }

    /**
     * 管理员查看所有车位（支持搜索）
     * @param page 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词（可搜索小区名称、车位编号、描述）
     * @return 车位列表
     */
    public Map<String, Object> adminList(int page, int pageSize, String keyword) {
        Page<ParkingSpot> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<ParkingSpot> q = new LambdaQueryWrapper<>();
        
        // 如果有关键词，进行搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 搜索小区名称
            List<Long> cids = communityMapper.selectList(
                    new LambdaQueryWrapper<Community>().like(Community::getName, keyword)
            ).stream().map(Community::getId).collect(Collectors.toList());
            
            if (!cids.isEmpty()) {
                // 如果找到匹配的小区，搜索这些小区的车位
                q.and(wrapper -> {
                    wrapper.in(ParkingSpot::getCommunityId, cids)
                           .or()
                           .like(ParkingSpot::getSpotNumber, keyword)
                           .or()
                           .like(ParkingSpot::getDescription, keyword);
                });
            } else {
                // 如果没有匹配的小区，只搜索车位编号和描述
                q.and(wrapper -> {
                    wrapper.like(ParkingSpot::getSpotNumber, keyword)
                           .or()
                           .like(ParkingSpot::getDescription, keyword);
                });
            }
        }
        
        // 按创建时间倒序
        q.orderByDesc(ParkingSpot::getCreateTime);

        Page<ParkingSpot> result = parkingSpotMapper.selectPage(p, q);
        List<Map<String, Object>> list = toDtoList(result.getRecords(), null, null, null);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("hasMore", result.hasNext());
        data.put("total", result.getTotal());
        return data;
    }

    private List<Map<String, Object>> toDtoList(List<ParkingSpot> list, BigDecimal lat, BigDecimal lng, BigDecimal maxDistance) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        Set<Long> cids = list.stream().map(ParkingSpot::getCommunityId).collect(Collectors.toSet());
        Map<Long, Community> cm = communityMapper.selectBatchIds(cids).stream().collect(Collectors.toMap(Community::getId, x -> x));
        List<Map<String, Object>> out = new ArrayList<>();
        for (ParkingSpot s : list) {
            Community c = cm.get(s.getCommunityId());
            if (c == null) continue;
            
            // 计算距离
            double distance = 0;
            if (lat != null && lng != null && c.getLatitude() != null && c.getLongitude() != null) {
                distance = calculateDistance(lat.doubleValue(), lng.doubleValue(), 
                                            c.getLatitude().doubleValue(), c.getLongitude().doubleValue());
                
                // 如果设置了最大距离，过滤超出范围的车位
                if (maxDistance != null && distance > maxDistance.doubleValue()) {
                    continue;
                }
            }
            
            Map<String, Object> dto = toDto(s, lat, lng, distance);
            dto.put("communityName", c.getName());
            out.add(dto);
        }
        
        // 按距离排序（用数值 distanceNum 排序，前端展示用字符串 distance）
        if (lat != null && lng != null) {
            out.sort((a, b) -> {
                Double d1 = a.get("distanceNum") != null ? ((Number) a.get("distanceNum")).doubleValue() : Double.MAX_VALUE;
                Double d2 = b.get("distanceNum") != null ? ((Number) b.get("distanceNum")).doubleValue() : Double.MAX_VALUE;
                return d1.compareTo(d2);
            });
        }
        
        // 限制返回数量
        if (out.size() > 10) {
            out = out.subList(0, 10);
        }
        
        return out;
    }
    
    /**
     * 计算两点之间的距离（公里）
     * 使用 Haversine 公式
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // 地球半径（公里）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Map<String, Object> toDto(ParkingSpot s, BigDecimal lat, BigDecimal lng, double distance) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", s.getId());
        m.put("communityId", s.getCommunityId());
        m.put("spotNumber", s.getSpotNumber());
        m.put("pricePerHour", s.getPricePerHour());
        m.put("description", s.getDescription());
        m.put("status", s.getStatus());
        m.put("statusText", STATUS_TEXT.getOrDefault(s.getStatus(), s.getStatus()));
        if (s.getImages() != null && !s.getImages().trim().isEmpty()) {
            m.put("images", JSON.parseArray(s.getImages()));
        } else {
            m.put("images", Collections.emptyList());
        }
        // 距离：数值供排序用，字符串供前端展示
        if (distance > 0) {
            m.put("distanceNum", distance);
            if (distance < 1) {
                m.put("distance", String.format("%.0f", distance * 1000) + "m");
            } else {
                m.put("distance", String.format("%.2f", distance) + "km");
            }
        }
        return m;
    }
}
