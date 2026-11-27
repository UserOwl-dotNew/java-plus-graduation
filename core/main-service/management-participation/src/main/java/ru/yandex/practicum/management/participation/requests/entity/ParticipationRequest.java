package ru.yandex.practicum.management.participation.requests.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.yandex.practicum.common.entity.Event;
import ru.yandex.practicum.common.entity.User;
import ru.yandex.practicum.common.enums.RequestStatus;

import java.time.LocalDateTime;

/**
 * Сущность запроса на участие в событии.
 * <p>
 * Запрос создается пользователем для участия в событии.
 * Может находиться в одном из статусов: PENDING, CONFIRMED, REJECTED, CANCELED.
 * </p>
 */
@Entity
@Table(name = "participation_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Событие, на которое подается запрос.
     * Связь многие-к-одному с таблицей events.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Event event;

    /**
     * Пользователь, подавший запрос на участие.
     * Связь многие-к-одному с таблицей users.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User requester;

    /**
     * Текущий статус запроса.
     * Возможные значения: PENDING, CONFIRMED, REJECTED, CANCELED.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    /**
     * Дата и время создания запроса.
     */
    @Column(nullable = false)
    private LocalDateTime created;
}