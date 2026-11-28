package ru.yandex.practicum.statistics.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "ru.yandex.practicum")
@EntityScan(basePackages = {
        "ru.yandex.practicum.statistics.server.model",
        "ru.yandex.practicum.common",
        "ru.yandex.practicum.dto",
        "ru.yandex.practicum.statistics.client"
})
public class StatisticsServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatisticsServerApplication.class);
    }
}
