package com.harness;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class AppInfoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void recordHoldsFields() {
        AppInfo info = new AppInfo("app", "up", "1.0.0", "abc", "build-1");
        assertThat(info.app()).isEqualTo("app");
        assertThat(info.status()).isEqualTo("up");
        assertThat(info.version()).isEqualTo("1.0.0");
        assertThat(info.gitSha()).isEqualTo("abc");
        assertThat(info.buildId()).isEqualTo("build-1");
    }

    @Test
    void serializesToJson() throws JsonProcessingException {
        AppInfo info = new AppInfo("BNS Spring Boot Sample", "running", "dev", "local", "—");
        String json = mapper.writeValueAsString(info);
        assertThat(json).contains("BNS Spring Boot Sample");
        assertThat(json).contains("running");
        assertThat(json).contains("gitSha");
        AppInfo roundTrip = mapper.readValue(json, AppInfo.class);
        assertThat(roundTrip).isEqualTo(info);
    }
}
