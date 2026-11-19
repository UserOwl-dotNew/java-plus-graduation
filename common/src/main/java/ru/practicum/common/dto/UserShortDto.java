package ru.practicum.common.dto;

import lombok.*;

/**
 * DTO для краткой информации о пользователе.
 * Используется в ответах API для отображения основных данных без email.
 */
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserShortDto {

    /**
     * Идентификатор пользователя.
     */
    private Long id;

    /**
     * Имя пользователя.
     */
    private String name;
}