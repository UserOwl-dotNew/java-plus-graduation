package ru.yandex.practicum.management.participation.requests.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.common.dto.EventFullDto;
import ru.yandex.practicum.common.entity.Category;
import ru.yandex.practicum.common.entity.Event;
import ru.yandex.practicum.common.entity.Location;
import ru.yandex.practicum.common.entity.User;
import ru.yandex.practicum.common.enums.EventState;
import ru.yandex.practicum.common.enums.RequestStatus;
import ru.yandex.practicum.common.exception.ConflictException;
import ru.yandex.practicum.common.exception.NotFoundException;
import ru.yandex.practicum.common.openfeign.EventFeignClient;
import ru.yandex.practicum.management.participation.requests.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.management.participation.requests.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.management.participation.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.management.participation.requests.entity.ParticipationRequest;
import ru.yandex.practicum.management.participation.requests.repository.ParticipationRequestRepository;
import ru.yandex.practicum.management.participation.requests.service.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventFeignClient eventClient;

    @Override
    @Transactional(readOnly = true)
    public long getConfirmedCount(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findByRequestId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        Event event = toEventEntity(eventClient.getEvent(eventId));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
        if (event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать заявку на участие");
        }
        if (requestRepository.findByEventIdAndRequestId(eventId, userId).isPresent()) {
            throw new ConflictException("Запрос на участие уже существует");
        }
        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setEventId(event.getId());

        User requester = new User();
        requester.setId(userId);
        request.setRequestId(requester.getId());

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }
        request.setCreated(LocalDateTime.now());

        ParticipationRequest saved = requestRepository.save(request);
        log.info("Добавлен запрос на участие: {}", saved.getId());
        return toDto(saved);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        if (!request.getRequestId().equals(userId)) {
            throw new ConflictException("Нельзя отменить чужой запрос");
        }
        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updated = requestRepository.save(request);
        log.info("Отменён запрос: {}", updated.getId());
        return toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = toEventEntity(eventClient.getEvent(eventId));
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Только инициатор события может видеть заявки");
        }
        return requestRepository.findByEventId(eventId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        Event event = toEventEntity(eventClient.getEvent(eventId));
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Только инициатор может менять статус заявок");
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников уже достигнут");
        }

        List<ParticipationRequest> requests = requestRepository.findByEventIdAndIdIn(eventId, updateRequest.getRequestIds());
        if (requests.size() != updateRequest.getRequestIds().size()) {
            throw new NotFoundException("Некоторые заявки не найдены или не принадлежат событию");
        }

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest req : requests) {
            if (!req.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Можно менять статус только у заявок со статусом PENDING");
            }

            if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
                if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
                    req.setStatus(RequestStatus.REJECTED);
                    rejected.add(toDto(req));
                } else {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmedCount++;
                    confirmed.add(toDto(req));
                }
            } else if (updateRequest.getStatus() == RequestStatus.REJECTED) {
                req.setStatus(RequestStatus.REJECTED);
                rejected.add(toDto(req));
            }
        }

        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            List<ParticipationRequest> remaining = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
            for (ParticipationRequest req : remaining) {
                req.setStatus(RequestStatus.REJECTED);
                rejected.add(toDto(req));
            }
        }

        requestRepository.saveAll(requests);
        log.info("Обновлены статусы заявок для события {}", eventId);
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    private ParticipationRequestDto toDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getEventId(),
                request.getRequestId(),
                request.getStatus(),
                request.getCreated()
        );
    }

    private Event toEventEntity(EventFullDto dto) {
        return Event.builder()
                .id(dto.getId())
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .title(dto.getTitle())
                .category(new Category(
                        dto.getCategory().getId(),
                        dto.getCategory().getName()
                ))
                .initiatorId(dto.getInitiator().getId())
                .location(new Location (
                        dto.getLocation().getLat(),
                        dto.getLocation().getLon()
                ))
                .eventDate(dto.getEventDate())
                .createdOn(dto.getCreatedOn())
                .publishedOn(dto.getPublishedOn())
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .state(dto.getState())
                .build();
    }
}
