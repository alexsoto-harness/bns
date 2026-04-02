package com.harness;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    // Intentional dummy values for secret-scanner demos (not real credentials).
    private static final String GITHUB_TOKEN = "ghp_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8";
    private static final String AWS_ACCESS_KEY_ID = "AKIAIOSFODNN7EXAMPLE";
    private static final String AWS_SECRET_ACCESS_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    private final AppEnv appEnv;

    HelloController(AppEnv appEnv) {
        this.appEnv = appEnv;
    }

    // version: JAR manifest. gitSha/buildId: env on pod — see README and k8s/values.yaml (Harness / ConfigMap).
    @GetMapping("/api/info")
    public AppInfo info() {
        Package pkg = Springbootsample.class.getPackage();
        String version = Optional.ofNullable(pkg.getImplementationVersion()).orElse("dev");
        String gitSha = firstNonBlank(
                appEnv.var("GIT_COMMIT_SHA"),
                appEnv.var("GITHUB_SHA"),
                appEnv.var("COMMIT_SHA"),
                "local");
        String buildId = firstNonBlank(appEnv.var("BUILD_ID"), appEnv.var("RUN_NUMBER"), "—");
        return new AppInfo("BNS Spring Boot Sample", "running", version, gitSha, buildId);
    }

    @GetMapping("/api/config")
    public Map<String, Object> config() {
        boolean enableDarkMode = Boolean.parseBoolean(appEnv.varOrDefault("ENABLE_DARK_MODE", "false"));
        return Map.of("enableDarkMode", enableDarkMode);
    }

    @PostMapping("/api/contact")
    public ResponseEntity<Void> contact(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim();
        if (email.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        log.info("Contact demo submission email={} message={}", email, body.getOrDefault("message", ""));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    static String firstNonBlank(String... values) {
        if (values == null) {
            return "—";
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return "—";
    }
}
