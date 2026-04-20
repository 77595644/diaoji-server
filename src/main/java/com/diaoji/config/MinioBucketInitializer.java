package com.diaoji.config;

import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * MinIO Bucket 初始化：创建 bucket 并设置公开策略
 */
@Configuration
public class MinioBucketInitializer {

    private static final Logger log = LoggerFactory.getLogger(MinioBucketInitializer.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private static final String PUBLIC_POLICY = """
        {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Principal": {"AWS": ["*"]},
              "Action": ["s3:GetObject"],
              "Resource": ["arn:aws:s3:::%s/*"]
            }
          ]
        }
        """;

    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                log.info("创建 MinIO bucket: {}", bucketName);
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String desiredPolicy = String.format(PUBLIC_POLICY, bucketName);
            String currentPolicy;
            try {
                currentPolicy = minioClient.getBucketPolicy(
                        GetBucketPolicyArgs.builder().bucket(bucketName).build());
            } catch (Exception e) {
                currentPolicy = "";
            }

            if (!desiredPolicy.equals(currentPolicy)) {
                log.info("设置 bucket {} 公开读取策略", bucketName);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(desiredPolicy)
                                .build());
            } else {
                log.info("bucket {} 已是公开状态", bucketName);
            }

            log.info("✅ MinIO 初始化完成: {}", bucketName);

        } catch (Exception e) {
            log.error("❌ MinIO 初始化失败: {}", e.getMessage());
        }
    }
}
