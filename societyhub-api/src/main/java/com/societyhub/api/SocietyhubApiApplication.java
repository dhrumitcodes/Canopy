package com.societyhub.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SocietyhubApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocietyhubApiApplication.class, args);
    }

}
