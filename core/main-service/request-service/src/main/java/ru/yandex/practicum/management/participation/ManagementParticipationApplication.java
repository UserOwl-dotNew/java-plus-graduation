package ru.yandex.practicum.management.participation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.management.participation",
        "ru.yandex.practicum.common"
})
@EntityScan(basePackages = {
        "ru.yandex.practicum.common.entity",
        "ru.yandex.practicum.management.participation"
})
@EnableJpaRepositories(basePackages = {
        "ru.yandex.practicum.common.repository",
        "ru.yandex.practicum.management.participation"
})
@EnableFeignClients(basePackages = {
        "ru.yandex.practicum.common.openfeign"
})
public class ManagementParticipationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManagementParticipationApplication.class, args);
    }
}