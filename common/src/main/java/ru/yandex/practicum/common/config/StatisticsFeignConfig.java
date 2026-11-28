package ru.yandex.practicum.common.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;

public class StatisticsFeignConfig {

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(3000, 5000, 3);
    }
}
