package ru.practicum.statistics.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "ru.practicum")
@EntityScan(basePackages = {
        "ru.practicum.statistics.server.model",
        "ru.practicum.common",
        "ru.practicum.dto",
        "ru.practicum.statistics.client"
})
public class StatisticsServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatisticsServerApplication.class);
    }
}
