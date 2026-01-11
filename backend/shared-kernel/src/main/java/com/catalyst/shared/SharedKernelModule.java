package com.catalyst.shared;

/**
 * Marker class for the Shared Kernel module.
 * Contains cross-cutting concerns: JWT, Security, Exception Handling, Rate Limiting.
 */
public final class SharedKernelModule {
    
    public static final String MODULE_NAME = "shared-kernel";
    public static final String BASE_PACKAGE = "com.catalyst.shared";
    
    private SharedKernelModule() {
        // Utility class - prevent instantiation
    }
}

