package ru.yandex.practicum.user.administration.users.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.common.dto.UserDto;
import ru.yandex.practicum.common.entity.User;
import ru.yandex.practicum.user.administration.users.dto.NewUserRequest;

import java.util.List;

/**
 * Сервис для управления пользователями.
 * Предоставляет методы для получения, создания и удаления пользователей,
 * а также для получения JPA-сущности пользователя для внутренних вызовов.
 */
public interface UserService {

    /**
     * Получение пользователей с фильтрацией по идентификаторам и пагинацией.
     * Если идентификаторы не переданы, возвращаются все пользователи.
     *
     * @param ids      список идентификаторов пользователей (опционально)
     * @param pageable параметры пагинации
     * @return список пользователей
     */
    List<UserDto> getUsers(List<Long> ids, Pageable pageable);

    /**
     * Создание нового пользователя.
     *
     * @param request данные нового пользователя
     * @return созданный пользователь
     */
    UserDto createUser(NewUserRequest request);

    /**
     * Удаление пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     */
    void deleteUser(Long userId);

    /**
     * Получение JPA-сущности пользователя для внутренних вызовов между сервисами.
     *
     * @param userId идентификатор пользователя
     * @return сущность пользователя
     */
    User getUserEntity(Long userId);

    Boolean existsById(Long id);
}