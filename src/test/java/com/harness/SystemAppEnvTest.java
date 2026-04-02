package com.harness;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SystemAppEnvTest {

    private final SystemAppEnv env = new SystemAppEnv();

    @Test
    void var_returnsNullForUnknownKey() {
        assertThat(env.var("BNS_TEST_UNKNOWN_ENV_VAR_XYZ")).isNull();
    }

    @Test
    void varOrDefault_returnsDefaultWhenUnset() {
        assertThat(env.varOrDefault("BNS_TEST_UNKNOWN_ENV_VAR_XYZ", "fallback")).isEqualTo("fallback");
    }

    @Test
    void varOrDefault_returnsValueWhenSet() {
        String path = env.var("PATH");
        assertThat(path).isNotNull();
        assertThat(env.varOrDefault("PATH", "none")).isEqualTo(path);
    }
}
