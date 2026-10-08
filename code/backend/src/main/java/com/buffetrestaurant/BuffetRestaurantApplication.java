package com.buffetrestaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BuffetRestaurantApplication {
    public static void main(String[] args) {
        DatabaseTlsDiagnostic.runWhenEnabled();
        SpringApplication.run(BuffetRestaurantApplication.class, args);
    }
}
