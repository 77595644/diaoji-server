package com.diaoji.util;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * MinIO 文件上传工具
 */
@Component
public class MinioUploader {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ObjectMapper objectMapper;

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

    /**
     * 从公网 URL 中提取 objectName 并删除 MinIO 文件
     * @param publicUrl  公网访问 URL，格式如 http://localhost:9000/diaoji/catch/xxx.jpg
     */
    public void deleteFile(String publicUrl) {
        if (publicUrl == null || publicUrl.isEmpty()) return;
        try {
            // 提取实际存储路径（去 publicHost 和 bucketName 前缀）
            String objectName;
            if (publicHost != null && publicHost.endsWith("/")) {
                // publicHost 如 http://localhost:9000，URL 是 http://localhost:9000/diaoji/catch/xxx.png
                // 去掉 host 后剩 diaoji/catch/xxx.png，再去掉 diaoji/ 前缀才是真正 objectName
                objectName = publicUrl.replace(publicHost, "");
                if (objectName.startsWith(bucketName + "/")) {
                    objectName = objectName.substring(bucketName.length() + 1);
                }
            } else if (publicHost != null) {
                objectName = publicUrl.replace(publicHost + "/", "");
                if (objectName.startsWith(bucketName + "/")) {
                    objectName = objectName.substring(bucketName.length() + 1);
                }
            } else {
                int idx = publicUrl.indexOf("/" + bucketName + "/");
                if (idx < 0) return;
                objectName = publicUrl.substring(idx + ("/" + bucketName + "/").length());
            }

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            System.err.println("[MinioUploader] 删除文件失败: " + publicUrl + " | " + e.getMessage());
        }
    }

    /**
     * 根据公网 URL 列表批量删除文件
     */
    public void deleteFiles(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        for (String url : urls) {
            deleteFile(url);
        }
    }

    /**
     * 从数据库 photos JSON 字符串解析出 URL 列表
     * @param photosJson  如 ["url1","url2"] 或 null
     */
    public List<String> parsePhotoUrls(String photosJson) {
        if (photosJson == null || photosJson.isEmpty()) return Collections.emptyList();
        try {
            @SuppressWarnings("unchecked")
            List<String> list = objectMapper.readValue(photosJson, List.class);
            return list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
