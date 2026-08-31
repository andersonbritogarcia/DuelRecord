package com.duelrecord.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZonedDateTime;
import java.util.TimeZone;

@SpringBootApplication
public class AppApplication {

    private static final Logger log = LoggerFactory.getLogger(AppApplication.class);

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(AppApplication.class, args);
        log.info("Duel Record started at {}", ZonedDateTime.now());
    }
}
