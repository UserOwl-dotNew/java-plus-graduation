package ru.practicum.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO для передачи полной информации о пользователе.
 */
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
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