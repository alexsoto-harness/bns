package com.harness;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppEnv appEnv;

    @BeforeEach
    void defaultEnvironment() {
        lenient().when(appEnv.var(anyString())).thenReturn(null);
        lenient().when(appEnv.varOrDefault(eq("ENABLE_DARK_MODE"), eq("false"))).thenReturn("false");
    }

    @Test
    void info_returnsExpectedAppAndStatus() throws Exception {
        mockMvc.perform(get("/api/info").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.app", is("BNS Spring Boot Sample")))
                .andExpect(jsonPath("$.status", is("running")))
                .andExpect(jsonPath("$.gitSha", is("local")))
                .andExpect(jsonPath("$.version", not(emptyString())))
                .andExpect(jsonPath("$.buildId", is("—")));
    }

    @Test
    void info_prefersGitCommitShaOverGithubSha() throws Exception {
        when(appEnv.var("GIT_COMMIT_SHA")).thenReturn("aaa111");
        when(appEnv.var("GITHUB_SHA")).thenReturn("bbb222");

        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gitSha", is("aaa111")));
    }

    @Test
    void info_fallsBackToGithubShaWhenGitCommitShaBlank() throws Exception {
        when(appEnv.var("GIT_COMMIT_SHA")).thenReturn("   ");
        when(appEnv.var("GITHUB_SHA")).thenReturn("fullsha");

        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gitSha", is("fullsha")));
    }

    @Test
    void info_fallsBackToCommitSha() throws Exception {
        when(appEnv.var("COMMIT_SHA")).thenReturn("onlycommit");

        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gitSha", is("onlycommit")));
    }

    @Test
    void info_prefersBuildIdOverRunNumber() throws Exception {
        when(appEnv.var("BUILD_ID")).thenReturn("exec-uuid");
        when(appEnv.var("RUN_NUMBER")).thenReturn("42");

        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buildId", is("exec-uuid")));
    }

    @Test
    void info_usesRunNumberWhenBuildIdBlank() throws Exception {
        when(appEnv.var("BUILD_ID")).thenReturn("");
        when(appEnv.var("RUN_NUMBER")).thenReturn("99");

        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buildId", is("99")));
    }

    @Test
    void config_enableDarkMode_falseByDefault() throws Exception {
        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enableDarkMode", is(false)));
    }

    @Test
    void config_enableDarkMode_trueWhenEnvSet() throws Exception {
        when(appEnv.varOrDefault(eq("ENABLE_DARK_MODE"), eq("false"))).thenReturn("true");

        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enableDarkMode", is(true)));
    }

    @Test
    void config_enableDarkMode_caseInsensitiveTrue() throws Exception {
        when(appEnv.varOrDefault(eq("ENABLE_DARK_MODE"), eq("false"))).thenReturn("TRUE");

        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enableDarkMode", is(true)));
    }

    @Test
    void contact_returns202WhenEmailPresent() throws Exception {
        var body = objectMapper.createObjectNode();
        body.put("email", "user@example.com");
        body.put("message", "hello");

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isAccepted());
    }

    @Test
    void contact_returns202WhenMessageOmitted() throws Exception {
        var body = objectMapper.createObjectNode();
        body.put("email", "a@b.co");

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isAccepted());
    }

    @Test
    void contact_returns400WhenEmailMissing() throws Exception {
        var body = objectMapper.createObjectNode();
        body.put("message", "no email");

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void contact_returns400WhenEmailBlank() throws Exception {
        var body = objectMapper.createObjectNode();
        body.put("email", "   ");

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void contact_returns400ForEmptyObject() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
