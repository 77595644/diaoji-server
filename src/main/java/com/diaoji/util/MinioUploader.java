package com.diaoji.util;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * MinIO 文件上传工具
 */
@Component
public class MinioUploader {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.public-host:http://localhost:9000}")
    private String publicHost;

    /**
     * 上传 Base64 图片，返回公网 URL
     * @param base64Data  data:image/png;base64,xxxxx 或纯 base64 字符串
     * @param folder      目录，如 "catch/" "poster/"
     * @return 公网访问 URL
     */
    public String uploadBase64(String base64Data, String folder) {
        if (base64Data == null || base64Data.isEmpty()) {
            return null;
        }

        // 去掉 data:image 前缀
        if (base64Data.contains(",")) {
            base64Data = base64Data.split(",")[1];
        }

        byte[] bytes = Base64.getDecoder().decode(base64Data);
        String suffix = detectSuffix(bytes);
        String objectName = folder + UUID.randomUUID().toString().replace("-", "") + suffix;

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            // 确保 bucket 存在
            boolean exists = false;
            try {
                minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
                exists = true;
            } catch (Exception e) {
                // 不存在，正常
            }

            if (!exists) {
                // bucket 初始化需要手动创建，这里先尝试创建
                try {
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(bais, bytes.length, -1)
                            .contentType(getContentType(suffix))
                            .build());
                } catch (Exception bucketErr) {
                    // bucket 不存在时创建
                    try {
                        minioClient.makeBucket(
                                io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
                        bais.reset();
                        minioClient.putObject(PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(bais, bytes.length, -1)
                                .contentType(getContentType(suffix))
                                .build());
                    } catch (Exception createErr) {
                        throw new RuntimeException("MinIO 上传失败: " + createErr.getMessage(), createErr);
                    }
                }
            } else {
                bais.reset();
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(bais, bytes.length, -1)
                        .contentType(getContentType(suffix))
                        .build());
            }

            return publicHost + "/" + bucketName + "/" + objectName;

        } catch (Exception e) {
            throw new RuntimeException("MinIO 上传失败: " + e.getMessage(), e);
        }
    }

    private String detectSuffix(byte[] bytes) {
        // PNG: 89 50 4E 47, JPEG: FF D8 FF, GIF: 47 49 46 38
        if (bytes.length >= 4) {
            if (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50) return ".png";
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) return ".jpg";
            if (bytes[0] == (byte) 0x47 && bytes[1] == (byte) 0x49) return ".gif";
            if (bytes[0] == (byte) 0x57 && bytes[1] == (byte) 0x45 && bytes[2] == (byte) 0x42) return ".webp";
        }
        return ".jpg";
    }

    private String getContentType(String suffix) {
        return switch (suffix.toLowerCase()) {
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}
