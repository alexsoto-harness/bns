package com.harness;

import org.springframework.stereotype.Component;

@Component
class SystemAppEnv implements AppEnv {

    @Override
    public String var(String name) {
        return System.getenv(name);
    }
}
