package com.harness;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HelloControllerFirstNonBlankTest {

    @Test
    void returnsEmDashWhenNullArray() {
        assertThat(HelloController.firstNonBlank((String[]) null)).isEqualTo("—");
    }

    @Test
    void returnsEmDashWhenAllNullOrBlank() {
        assertThat(HelloController.firstNonBlank(null, "", "  ", "\t")).isEqualTo("—");
    }

    @Test
    void returnsFirstNonBlank() {
        assertThat(HelloController.firstNonBlank(null, "  ", "first", "second")).isEqualTo("first");
    }

    @Test
    void singleValue() {
        assertThat(HelloController.firstNonBlank("only")).isEqualTo("only");
    }
}
