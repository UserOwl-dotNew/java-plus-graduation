package ru.yandex.practicum.user.administration.controller.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.common.dto.UserDto;
import ru.yandex.practicum.user.administration.controller.api.user.AdminUserControllerApi;
import ru.yandex.practicum.user.administration.users.dto.NewUserRequest;
import ru.yandex.practicum.user.administration.users.service.UserService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController implements AdminUserControllerApi {

    private final UserService userService;

    @Override
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero int from,
            @RequestParam(defaultValue = "10")
            @Positive int size
    ) {
        log.info(
                "GET /admin/users with ids={}, from={}, size={}",
                ids,
                from,
                size
        );

        return userService.getUsers(
                ids,
                PageRequest.of(from / size, size)
        );
    }

    @Override
    public UserDto createUser(
            @Valid @RequestBody NewUserRequest request
    ) {
        log.info("POST /admin/users with request: {}", request);

        return userService.createUser(request);
    }

    @Override
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{}", userId);

        userService.deleteUser(userId);
    }
}
