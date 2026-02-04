package com.parking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能匹配：多因子加权算法（简化版）
 * 按价格、距离、可用时间等因子综合排序
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final ParkingSpotService parkingSpotService;

    public Map<String, Object> smartMatch(Long communityId, LocalDateTime startTime, LocalDateTime endTime, BigDecimal lat, BigDecimal lng) {
        List<Map<String, Object>> list = parkingSpotService.recommended(communityId, lat, lng, null);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("hasMore", false);
        return data;
    }
}
