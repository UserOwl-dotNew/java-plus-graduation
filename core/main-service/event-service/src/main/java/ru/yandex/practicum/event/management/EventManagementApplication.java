package ru.yandex.practicum.event.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.event.management",
        "ru.yandex.practicum.common"
})
@EntityScan(basePackages = {
        "ru.yandex.practicum.common.entity",
        "ru.yandex.practicum.event.management"
})
@EnableJpaRepositories(basePackages = {
        "ru.yandex.practicum.event.management"
})
@EnableFeignClients(basePackages = {
        "ru.yandex.practicum.common.openfeign"
})
public class EventManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventManagementApplication.class, args);
    }
}
