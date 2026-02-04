package com.parking.controller;

import com.parking.common.Result;
import com.parking.entity.Community;
import com.parking.service.CommunityService;
import com.parking.service.TokenService;
import com.parking.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final TokenService tokenService;
    private final AuthUtil authUtil;

    @GetMapping("/list")
    public Result<List<Community>> list(@RequestParam(required = false) String keyword) {
        List<Community> list = communityService.list(keyword);
        return Result.ok(list);
    }

    /**
     * 添加小区（仅管理员）
     * name 小区名称，address 腾讯地图选点得到的实际地址名称，latitude/longitude 经纬度
     */
    @PostMapping("/add")
    public Result<Community> add(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> body) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        if (!authUtil.isAdmin(userId)) return Result.fail(403, "仅管理员可添加小区");

        String name = body.get("name") != null ? body.get("name").toString().trim() : null;
        String address = body.get("address") != null ? body.get("address").toString().trim() : null;
        BigDecimal latitude = null;
        BigDecimal longitude = null;
        if (body.get("latitude") != null) latitude = new BigDecimal(body.get("latitude").toString());
        if (body.get("longitude") != null) longitude = new BigDecimal(body.get("longitude").toString());

        if (name == null || name.isEmpty()) return Result.fail("请输入小区名称");
        if (address == null || address.isEmpty()) return Result.fail("请选择小区定位（地址）");
        if (latitude == null || longitude == null) return Result.fail("请选择小区定位（经纬度）");

        Community c = communityService.add(name, address, latitude, longitude);
        return Result.ok(c);
    }
}
