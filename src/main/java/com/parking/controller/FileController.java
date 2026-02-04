package com.parking.controller;

import com.parking.common.Result;
import com.parking.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 本地文件上传（图片）
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final TokenService tokenService;

    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");

        if (file == null || file.isEmpty()) return Result.fail("请选择文件");
        if (file.getSize() > 10 * 1024 * 1024L) return Result.fail("文件过大（最大10MB）");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            return Result.fail("仅支持图片上传");
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null) {
            int i = original.lastIndexOf('.');
            if (i >= 0 && i < original.length() - 1) ext = original.substring(i).toLowerCase();
        }
        if (ext.isEmpty()) ext = ".jpg";

        String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String baseDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + day + File.separator;
        File dir = new File(baseDir);
        if (!dir.exists() && !dir.mkdirs()) {
            return Result.fail("创建上传目录失败");
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        File dest = new File(dir, filename);
        file.transferTo(dest);

        String relativeUrl = "/uploads/" + day + "/" + filename; // 会挂在 /api/uploads/...（因为 context-path=/api）
        String fullUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath() + relativeUrl;

        Map<String, Object> data = new HashMap<>();
        data.put("url", relativeUrl);
        data.put("fullUrl", fullUrl);
        return Result.ok(data);
    }
}

