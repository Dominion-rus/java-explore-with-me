package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.ewm",
        "ru.practicum.stats.client",
        "ru.practicum.stats.dto"
})
public class EwmServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EwmServerApplication.class, args);
    }
}
