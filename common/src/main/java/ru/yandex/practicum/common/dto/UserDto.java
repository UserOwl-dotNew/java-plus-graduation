package ru.yandex.practicum.common.dto;

import lombok.*;

/**
 * DTO для передачи полной информации о пользователе.
 */
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    /**
     * Идентификатор пользователя.
     */
    private Long id;

    /**
     * Электронная почта пользователя.
     */
    private String email;

    /**
     * Имя пользователя.
     */
    private String name;
}