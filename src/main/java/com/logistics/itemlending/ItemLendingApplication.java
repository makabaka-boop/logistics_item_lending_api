package com.logistics.itemlending;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ItemLendingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItemLendingApplication.class, args);
    }
}
