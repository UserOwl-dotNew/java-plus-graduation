package ru.yandex.practicum.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.yandex.practicum.common.config.JacksonConfig;

import java.time.LocalDateTime;

/**
 * DTO для краткой информации о событии.
 * Используется в публичных запросах для отображения списка событий.
 */
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventShortDto {

    /**
     * Идентификатор события.
     */
    private Long id;

    /**
     * Краткое описание события (аннотация).
     */
    private String annotation;

    /**
     * Заголовок события.
     */
    private String title;

    /**
     * Категория события.
     */
    private CategoryDto category;

    /**
     * Краткая информация об инициаторе события.
     */
    private UserShortDto initiator;

    /**
     * Флаг платности участия.
     */
    private Boolean paid;

    /**
     * Количество подтвержденных заявок на участие.
     */
    private Long confirmedRequests;

    /**
     * Количество просмотров события.
     */
    private Long views;

    /**
     * Дата и время проведения события.
     */
    @JsonFormat(pattern = JacksonConfig.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;
}