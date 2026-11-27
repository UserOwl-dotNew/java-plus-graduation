package ru.yandex.practicum.common.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.common.dto.UserDto;

@FeignClient(name = "user-administration")
public interface UserFeignClient {
    @GetMapping("/internal/users/{id}")
    UserDto getEntity(@PathVariable("id") Long id);

    @GetMapping("/internal/users/{id}/exists")
    boolean existsById(@PathVariable("id") Long id);
}
