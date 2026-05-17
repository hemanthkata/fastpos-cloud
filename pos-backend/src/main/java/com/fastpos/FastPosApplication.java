package com.fastpos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FastPosApplication {

    public static void main(String[] args) {
        SpringApplication.run(FastPosApplication.class, args);
    }
}
