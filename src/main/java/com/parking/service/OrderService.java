package com.parking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parking.entity.*;
import com.parking.mapper.CommunityMapper;
import com.parking.mapper.OrderMapper;
import com.parking.mapper.ParkingSpotMapper;
import com.parking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final ParkingSpotMapper parkingSpotMapper;
    private final CommunityMapper communityMapper;
    private final UserMapper userMapper;

    private static final AtomicLong SEQ = new AtomicLong(1);
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Map<String, String> STATUS_TEXT = new HashMap<String, String>() {{
        put("pending", "待支付");
        put("ongoing", "进行中");
        put("completed", "已完成");
        put("cancelled", "已取消");
    }};

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, Long parkingSpotId, LocalDateTime startTime, LocalDateTime endTime) {
        ParkingSpot spot = parkingSpotMapper.selectById(parkingSpotId);
        if (spot == null || !"available".equals(spot.getStatus())) return null;
        int hours = (int) Math.ceil(java.time.Duration.between(startTime, endTime).toMinutes() / 60.0);
        if (hours <= 0) return null;
        BigDecimal total = spot.getPricePerHour().multiply(BigDecimal.valueOf(hours));

        String orderNumber = "ORD" + LocalDateTime.now().format(F) + String.format("%04d", SEQ.incrementAndGet() % 10000);
        Order o = new Order();
        o.setOrderNumber(orderNumber);
        o.setUserId(userId);
        o.setParkingSpotId(parkingSpotId);
        o.setStartTime(startTime);
        o.setEndTime(endTime);
        o.setHours(hours);
        o.setPricePerHour(spot.getPricePerHour());
        o.setTotalPrice(total);
        o.setStatus("ongoing");
        orderMapper.insert(o);

        spot.setStatus("reserved");
        parkingSpotMapper.updateById(spot);
        return o.getId();
    }

    public Map<String, Object> list(Long userId, int page, int pageSize, String status) {
        Page<Order> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Order> q = new LambdaQueryWrapper<>();
        q.eq(Order::getUserId, userId);
        if (status != null && !status.trim().isEmpty() && !"all".equals(status)) {
            q.eq(Order::getStatus, status);
        }
        q.orderByDesc(Order::getCreateTime);
        Page<Order> result = orderMapper.selectPage(p, q);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Order o : result.getRecords()) {
            list.add(toDto(o));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("hasMore", result.hasNext());
        return data;
    }

    public Map<String, Object> getById(Long id, Long userId) {
        Order o = orderMapper.selectById(id);
        if (o == null || !o.getUserId().equals(userId)) return null;
        return toDto(o);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long id, Long userId) {
        Order o = orderMapper.selectById(id);
        if (o == null || !o.getUserId().equals(userId)) return false;
        if (!"pending".equals(o.getStatus()) && !"ongoing".equals(o.getStatus())) return false;
        o.setStatus("cancelled");
        orderMapper.updateById(o);
        ParkingSpot s = parkingSpotMapper.selectById(o.getParkingSpotId());
        if (s != null && "reserved".equals(s.getStatus())) {
            s.setStatus("available");
            parkingSpotMapper.updateById(s);
        }
        return true;
    }

    /** 完成停车：进行中 → 待支付，释放车位 */
    @Transactional(rollbackFor = Exception.class)
    public boolean complete(Long id, Long userId) {
        Order o = orderMapper.selectById(id);
        if (o == null || !o.getUserId().equals(userId) || !"ongoing".equals(o.getStatus())) return false;
        o.setStatus("pending");
        orderMapper.updateById(o);
        ParkingSpot s = parkingSpotMapper.selectById(o.getParkingSpotId());
        if (s != null && "reserved".equals(s.getStatus())) {
            s.setStatus("available");
            parkingSpotMapper.updateById(s);
        }
        return true;
    }

    /** 支付：待支付 → 已完成 */
    public boolean pay(Long id, Long userId) {
        Order o = orderMapper.selectById(id);
        if (o == null || !o.getUserId().equals(userId) || !"pending".equals(o.getStatus())) return false;
        o.setStatus("completed");
        orderMapper.updateById(o);
        return true;
    }

    private Map<String, Object> toDto(Order o) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", o.getId());
        m.put("orderNumber", o.getOrderNumber());
        m.put("status", o.getStatus());
        m.put("statusText", STATUS_TEXT.getOrDefault(o.getStatus(), o.getStatus()));
        m.put("startTime", o.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        m.put("endTime", o.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        m.put("hours", o.getHours());
        m.put("pricePerHour", o.getPricePerHour());
        m.put("totalPrice", o.getTotalPrice());
        m.put("createTime", o.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        ParkingSpot s = parkingSpotMapper.selectById(o.getParkingSpotId());
        if (s != null) {
            Community c = communityMapper.selectById(s.getCommunityId());
            if (c != null) m.put("communityName", c.getName());
            m.put("spotNumber", s.getSpotNumber());
            if (s.getImages() != null && !s.getImages().trim().isEmpty()) {
                try {
                    List<String> imgs = JSON.parseArray(s.getImages(), String.class);
                    if (imgs != null && !imgs.isEmpty()) m.put("parkingImage", imgs.get(0));
                } catch (Exception ignored) {}
            }
        }
        User owner = s != null ? userMapper.selectById(s.getOwnerId()) : null;
        if (owner != null) {
            m.put("ownerName", owner.getNickname());
            m.put("ownerPhone", owner.getPhone());
            m.put("ownerAvatar", owner.getAvatar());
        }
        return m;
    }
}
