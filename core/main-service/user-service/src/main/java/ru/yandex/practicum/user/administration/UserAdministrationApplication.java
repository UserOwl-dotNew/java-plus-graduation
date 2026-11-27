package ru.yandex.practicum.user.administration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.user.administration",
        "ru.yandex.practicum.common"
})
@EntityScan(basePackages = {
        "ru.yandex.practicum.common.entity",
        "ru.yandex.practicum.user.administration"
})
@EnableJpaRepositories(basePackages = {
        "ru.yandex.practicum.common.repository",
        "ru.yandex.practicum.user.administration"
})
@EnableFeignClients(basePackages = {
        "ru.yandex.practicum.common.openfeign"
})
public class UserAdministrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserAdministrationApplication.class, args);
    }
}
