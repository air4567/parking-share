package com.parking.controller;

import com.alibaba.fastjson2.JSON;
import com.parking.common.Result;
import com.parking.service.ParkingSpotService;
import com.parking.service.TokenService;
import com.parking.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parking")
@RequiredArgsConstructor
public class ParkingSpotController {

    private final ParkingSpotService parkingSpotService;
    private final TokenService tokenService;
    private final AuthUtil authUtil;

    @GetMapping("/recommended")
    public Result<List<Map<String, Object>>> recommended(
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(required = false) BigDecimal maxDistance) {
        List<Map<String, Object>> list = parkingSpotService.recommended(communityId, latitude, longitude, maxDistance);
        return Result.ok(list);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) String sortType) {
        Map<String, Object> data = parkingSpotService.list(page, pageSize, keyword, communityId, sortType);
        return Result.ok(data);
    }

    /** 我的车位、管理员列表等多段路径必须放在 /{id} 之前，否则会被误匹配 */
    @GetMapping("/my/list")
    public Result<Map<String, Object>> myList(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        Map<String, Object> data = parkingSpotService.myList(userId, page, pageSize);
        return Result.ok(data);
    }

    @GetMapping("/admin/list")
    public Result<Map<String, Object>> adminList(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        if (!authUtil.isAdmin(userId)) {
            return Result.fail(403, "无权限访问，仅管理员可查看");
        }
        Map<String, Object> data = parkingSpotService.adminList(page, pageSize, keyword);
        return Result.ok(data);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude) {
        Map<String, Object> data = parkingSpotService.getById(id, latitude, longitude);
        if (data == null) return Result.fail("车位不存在");
        return Result.ok(data);
    }

    @PostMapping("/publish")
    public Result<Map<String, Object>> publish(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> body) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        Object cid = body.get("communityId");
        Object sn = body.get("spotNumber");
        Object ph = body.get("pricePerHour");
        if (cid == null || sn == null || ph == null) return Result.fail("参数不完整");
        Long communityId = Long.valueOf(cid.toString());
        String spotNumber = sn.toString();
        BigDecimal pricePerHour = new BigDecimal(ph.toString());
        String description = body.containsKey("description") && body.get("description") != null ? body.get("description").toString().trim() : null;
        if (description != null && description.isEmpty()) description = null;
        Object imagesObj = body.get("images");
        String images = "[]";
        if (imagesObj != null) {
            if (imagesObj instanceof List) {
                images = JSON.toJSONString(imagesObj);
            } else {
                String s = imagesObj.toString().trim();
                if (s.startsWith("[")) images = s; else images = "[]";
            }
        }
        // 数据库 images 列长度限制，避免写入失败导致 500
        if (images != null && images.length() > 4096) {
            return Result.fail("图片数据过长，请减少图片数量或先上传到图床再填写链接");
        }
        Long id = parkingSpotService.publish(userId, communityId, spotNumber, pricePerHour, description, images);
        if (id == null) return Result.fail("发布失败");
        return Result.ok(Collections.singletonMap("id", id));
    }

    /**
     * 业主更新自己的车位信息（仅本人可改）
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        Object cid = body.get("communityId");
        Object sn = body.get("spotNumber");
        Object ph = body.get("pricePerHour");
        if (cid == null || sn == null || ph == null) return Result.fail("参数不完整");
        Long communityId = Long.valueOf(cid.toString());
        String spotNumber = sn.toString();
        BigDecimal pricePerHour = new BigDecimal(ph.toString());
        String description = body.containsKey("description") && body.get("description") != null ? body.get("description").toString().trim() : null;
        if (description != null && description.isEmpty()) description = null;
        Object imagesObj = body.get("images");
        String images = null;
        if (imagesObj != null) {
            if (imagesObj instanceof List) {
                images = JSON.toJSONString(imagesObj);
            } else {
                String s = imagesObj.toString().trim();
                if (s.startsWith("[")) images = s;
            }
        }
        if (images != null && images.length() > 4096) {
            return Result.fail("图片数据过长，请减少图片数量");
        }
        boolean ok = parkingSpotService.update(id, userId, communityId, spotNumber, pricePerHour, description, images);
        if (!ok) return Result.fail(403, "无权限修改该车位");
        return Result.ok(null);
    }
}
