package com.duelrecord.app;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationModulesTest {

    private final ApplicationModules modules = ApplicationModules.of(AppApplication.class);

    @Test
    void verifyModularity() {
        modules.verify();
    }

    @Test
    void printModules() {
        modules.forEach(System.out::println);
    }
}
