package com.parking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.entity.Community;
import com.parking.mapper.CommunityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityMapper communityMapper;

    public List<Community> list(String keyword) {
        LambdaQueryWrapper<Community> q = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            q.and(w -> w.like(Community::getName, keyword).or().like(Community::getAddress, keyword));
        }
        q.orderByAsc(Community::getId);
        return communityMapper.selectList(q);
    }

    /**
     * 添加小区（管理员），address 使用腾讯地图选点返回的实际地址名称
     */
    public Community add(String name, String address, BigDecimal latitude, BigDecimal longitude) {
        Community c = new Community();
        c.setName(name);
        c.setAddress(address);
        c.setLatitude(latitude);
        c.setLongitude(longitude);
        c.setTotalParkingSpots(0);
        communityMapper.insert(c);
        return c;
    }
}
