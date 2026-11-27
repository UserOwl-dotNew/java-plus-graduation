package ru.yandex.practicum.event.management.categories.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.common.dto.CategoryDto;
import ru.yandex.practicum.common.entity.Category;
import ru.yandex.practicum.common.exception.ConflictException;
import ru.yandex.practicum.common.exception.NotFoundException;
import ru.yandex.practicum.event.management.categories.dto.NewCategoryDto;

import java.util.List;

/**
 * Сервис для управления категориями событий
 */
public interface CategoryService {
    /**
     * Создание новой категории
     *
     * @param dto данные для создания категории
     * @return созданная категория
     * @throws ConflictException если категория с таким именем уже существует
     */
    CategoryDto createCategory(NewCategoryDto dto);

    /**
     * Обновление существующей категории
     *
     * @param catId идентификатор категории
     * @param dto   новые данные категории
     * @return обновленная категория
     * @throws NotFoundException если категория не найдена
     * @throws ConflictException если категория с таким именем уже существует
     */
    CategoryDto updateCategory(Long catId, CategoryDto dto);

    /**
     * Удаление категории
     *
     * @param catId идентификатор категории
     * @throws NotFoundException если категория не найдена
     * @throws ConflictException если категория связана с событиями
     */
    void deleteCategory(Long catId);

    /**
     * Получение списка всех категорий с пагинацией
     *
     * @param pageable параметры пагинации
     * @return список категорий
     */
    List<CategoryDto> getCategories(Pageable pageable);

    /**
     * Получение категории по идентификатору
     *
     * @param catId идентификатор категории
     * @return категория
     * @throws NotFoundException если категория не найдена
     */
    CategoryDto getCategory(Long catId);

    /**
     * Получение сущности категории по идентификатору
     * используется другими сервисами для проверки существования категории
     *
     * @param categoryId идентификатор категории
     * @return сущность категории
     * @throws NotFoundException если категория не найдена
     */
    Category getCategoryEntity(Long categoryId);
}
