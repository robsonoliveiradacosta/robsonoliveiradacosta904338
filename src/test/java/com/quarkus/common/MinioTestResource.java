package com.quarkus.common;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

public class MinioTestResource implements QuarkusTestResourceLifecycleManager {

    private MinIOContainer minio;

    @Override
    public Map<String, String> start() {
        minio = new MinIOContainer(DockerImageName.parse("minio/minio:RELEASE.2025-09-07T16-13-09Z"))
                .withUserName("minioadmin")
                .withPassword("minioadmin");

        minio.start();

        Map<String, String> props = new HashMap<>();
        props.put("quarkus.minio.host", minio.getS3URL());
        props.put("quarkus.minio.access-key", minio.getUserName());
        props.put("quarkus.minio.secret-key", minio.getPassword());
        props.put("quarkus.minio.secure", "false");

        // Evita conflitos/“lixo” entre execuções e deixa explícito que é bucket de teste
        props.put("app.minio.bucket", "album-images-test");

        return props;
    }

    @Override
    public void stop() {
        if (minio != null) {
            minio.stop();
        }
    }
}