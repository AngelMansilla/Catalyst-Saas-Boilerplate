package com.catalyst.shared.infrastructure.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DisplayName("Docker Connectivity Diagnostic")
class DockerDiagnostic {

    private static final Logger log = LoggerFactory.getLogger(DockerDiagnostic.class);

    @Container
    static final GenericContainer<?> redisContainer = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Test
    @DisplayName("Should start a redis container successfully")
    void shouldStartContainer() {
        log.info("Starting diagnostic container...");
        assertThat(redisContainer.isRunning()).isTrue();
    }
}
