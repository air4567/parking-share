package com.parking.controller;

import com.parking.common.Result;
import com.parking.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/smart-match")
    public Result<Map<String, Object>> smartMatch(@RequestBody Map<String, Object> body) {
        Long communityId = body.containsKey("communityId") && body.get("communityId") != null
                ? Long.valueOf(body.get("communityId").toString()) : null;
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (body.containsKey("startTime") && body.get("startTime") != null) {
            startTime = LocalDateTime.parse(body.get("startTime").toString().replace(" ", "T"));
        }
        if (body.containsKey("endTime") && body.get("endTime") != null) {
            endTime = LocalDateTime.parse(body.get("endTime").toString().replace(" ", "T"));
        }
        BigDecimal lat = body.containsKey("latitude") && body.get("latitude") != null
                ? new BigDecimal(body.get("latitude").toString()) : null;
        BigDecimal lng = body.containsKey("longitude") && body.get("longitude") != null
                ? new BigDecimal(body.get("longitude").toString()) : null;
        Map<String, Object> data = matchingService.smartMatch(communityId, startTime, endTime, lat, lng);
        return Result.ok(data);
    }
}
