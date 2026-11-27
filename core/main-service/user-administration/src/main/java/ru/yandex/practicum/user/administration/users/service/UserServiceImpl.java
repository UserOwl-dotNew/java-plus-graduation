package ru.yandex.practicum.user.administration.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.common.dto.UserDto;
import ru.yandex.practicum.common.entity.User;
import ru.yandex.practicum.common.exception.ConflictException;
import ru.yandex.practicum.common.exception.NotFoundException;
import ru.yandex.practicum.common.openfeign.EventFeignClient;
import ru.yandex.practicum.user.administration.users.dto.NewUserRequest;
import ru.yandex.practicum.user.administration.users.mapper.UserMapper;
import ru.yandex.practicum.user.administration.users.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EventFeignClient eventClient;

    @Override
    public List<UserDto> getUsers(
            List<Long> ids,
            Pageable pageable
    ) {
        log.info(
                "Getting users with ids={} and pageable={}",
                ids,
                pageable
        );

        Page<User> users;

        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findAllByIdIn(ids, pageable);
        }

        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        log.info("Creating user: {}", request);

        User user = userMapper.toEntity(request);

        try {
            User savedUser = userRepository.saveAndFlush(user);

            log.info("Created user with id={}", savedUser.getId());

            return userMapper.toDto(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(
                    "User with email '" + request.getEmail()
                            + "' already exists"
            );
        }
    }

    @Override
    public Boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id=" + userId + " was not found"
                ));

        if (eventClient.existsByInitiatorId(userId)) {
            throw new ConflictException("User with id=" + userId + " is initiator of events and cannot be deleted");
        }

        userRepository.delete(user);

        log.info("Deleted user with id={}", userId);
    }

    @Override
    public User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id=" + userId + " was not found"
                ));
    }
}
