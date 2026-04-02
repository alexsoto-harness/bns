package com.harness;

public record AppInfo(
        String app,
        String status,
        String version,
        String gitSha,
        String buildId
) {}
