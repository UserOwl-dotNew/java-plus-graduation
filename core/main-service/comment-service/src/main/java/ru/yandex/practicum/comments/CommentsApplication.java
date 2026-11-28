package ru.yandex.practicum.comments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.comments",
        "ru.yandex.practicum.common"
})
@EntityScan(basePackages = {
        "ru.yandex.practicum.common.entity",
        "ru.yandex.practicum.comments"
})
@EnableJpaRepositories(basePackages = {
        "ru.yandex.practicum.comments"
})
@EnableFeignClients(basePackages = {
        "ru.yandex.practicum.common.openfeign"
})
public class CommentsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommentsApplication.class, args);
    }
}
