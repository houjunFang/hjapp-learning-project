package com.hjapp.hjappmain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.hjapp")
public class HjappMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(HjappMainApplication.class, args);
    }

}
