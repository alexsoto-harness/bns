package com.harness;

/**
 * Abstraction over process environment variables for testability.
 */
public interface AppEnv {

    /** Returns {@code null} if the variable is unset. */
    String var(String name);

    default String varOrDefault(String name, String defaultValue) {
        String v = var(name);
        return v != null ? v : defaultValue;
    }
}
