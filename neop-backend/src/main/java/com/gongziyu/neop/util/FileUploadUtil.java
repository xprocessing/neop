package com.gongziyu.neop.util;

import com.gongziyu.neop.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Set;

/**
 * 文件上传校验工具类（文档22.4节）
 */
@Slf4j
@Component
public class FileUploadUtil {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;  // 10MB

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.of(400, "上传文件不能为空");
        }

        // 1. 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw BusinessException.of(400, "文件大小不能超过10MB");
        }

        // 2. 文件类型校验
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw BusinessException.of(400, "不支持的文件类型：" + contentType);
        }

        // 3. 文件扩展名校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw BusinessException.of(400, "文件名无效");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw BusinessException.of(400, "不支持的文件扩展名：" + extension);
        }

        // 4. 文件内容校验（防止恶意代码伪装为图片）
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw BusinessException.of(400, "文件内容不是有效的图片");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw BusinessException.of(400, "文件内容校验失败");
        }
    }
}
