package com.quarkus.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MinioStartup {

    private static final Logger LOG = Logger.getLogger(MinioStartup.class);

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "app.minio.bucket")
    String bucket;

    void onStart(@Observes StartupEvent event) {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build()
            );

            if (!exists) {
                LOG.infof("Creating MinIO bucket: %s", bucket);
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucket)
                        .build()
                );
                LOG.infof("MinIO bucket created successfully: %s", bucket);
            } else {
                LOG.infof("MinIO bucket already exists: %s", bucket);
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to create MinIO bucket: %s", bucket);
            throw new RuntimeException("Failed to initialize MinIO bucket", e);
        }
    }
}
