package com.parking.controller;

import com.parking.common.Result;
import com.parking.service.OrderService;
import com.parking.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final TokenService tokenService;

    @PostMapping("/create")
    public Result<Map<String, Object>> create(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, String> body) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        Long parkingSpotId = Long.valueOf(body.get("parkingSpotId"));
        LocalDateTime startTime = LocalDateTime.parse(body.get("startTime").replace(" ", "T"));
        LocalDateTime endTime = LocalDateTime.parse(body.get("endTime").replace(" ", "T"));
        Long id = orderService.create(userId, parkingSpotId, startTime, endTime);
        if (id == null) return Result.fail(400, "该时间段不可用，请选择其他时间");
        return Result.ok(Collections.singletonMap("id", id));
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        Map<String, Object> data = orderService.list(userId, page, pageSize, status);
        return Result.ok(data);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        Map<String, Object> data = orderService.getById(id, userId);
        if (data == null) return Result.fail("订单不存在");
        return Result.ok(data);
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        boolean ok = orderService.cancel(id, userId);
        if (!ok) return Result.fail("取消失败");
        return Result.ok(null);
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        boolean ok = orderService.complete(id, userId);
        if (!ok) return Result.fail("操作失败");
        return Result.ok(null);
    }

    @PutMapping("/{id}/pay")
    public Result<Void> pay(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        boolean ok = orderService.pay(id, userId);
        if (!ok) return Result.fail("支付失败");
        return Result.ok(null);
    }
}
