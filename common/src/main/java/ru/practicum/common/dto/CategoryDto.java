package ru.practicum.common.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO для передачи информации о категории событий.
 */
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    /**
     * Идентификатор категории.
     */
    private Long id;

    /**
     * Название категории. Максимальная длина 50 символов.
     */
    @Size(max = 50, message = "Название категории не должно превышать 50 символов")
    private String name;
}