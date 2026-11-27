package ru.yandex.practicum.user.administration.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.common.dto.UserDto;
import ru.yandex.practicum.user.administration.users.mapper.UserMapper;
import ru.yandex.practicum.user.administration.users.service.UserService;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;
    private final UserMapper mapper;

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return mapper.toDto(userService.getUserEntity(id));
    }

    @GetMapping("/{id}/exists")
    public boolean existsById(@PathVariable Long id) {
        return userService.existsById(id);
    }
}