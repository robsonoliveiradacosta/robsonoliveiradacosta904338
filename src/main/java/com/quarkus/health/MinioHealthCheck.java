package com.quarkus.health;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class MinioHealthCheck implements HealthCheck {

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "app.minio.bucket")
    String bucket;

    @Override
    public HealthCheckResponse call() {
        try {
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            return HealthCheckResponse.up("MinIO connection");
        } catch (Exception e) {
            return HealthCheckResponse.down("MinIO connection");
        }
    }
}
