package com.catalyst.shared;

import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import com.sun.jna.Native;

public class DockerDiagnostic {
    @Test
    public void testDockerAvailability() {
        System.out.println("DEBUG: JNA Version: " + Native.VERSION);
        try {
            System.out.println("DEBUG: Attempting to load JNA native...");
            Native.getNativeSize(long.class);
            System.out.println("DEBUG: JNA native loaded successfully.");
        } catch (Throwable t) {
            System.err.println("DEBUG: JNA native load FAILED!");
            t.printStackTrace();
        }

        try {
            System.out.println("DEBUG: Checking Docker environment via Testcontainers...");
            System.out.println("DEBUG: DOCKER_HOST system property: " + System.getProperty("DOCKER_HOST"));
            System.out.println("DEBUG: docker.host from properties should be: tcp://localhost:2375");

            boolean isDockerAvailable = DockerClientFactory.instance().isDockerAvailable();
            System.out.println("DEBUG: Docker available: " + isDockerAvailable);

            if (!isDockerAvailable) {
                System.out.println("DEBUG: Checking why Docker is not available...");
                // Attempt to get the client to see the exception
                try {
                    DockerClientFactory.instance().client();
                } catch (Exception e) {
                    System.err.println("DEBUG: Client creation FAILED!");
                    e.printStackTrace();
                }
            }
        } catch (Throwable t) {
            System.err.println("DEBUG: Docker available check FAILED!");
            t.printStackTrace();
        }
    }
}
