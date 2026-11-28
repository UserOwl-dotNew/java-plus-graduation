package ru.yandex.practicum.event.management.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.common.dto.EventFullDto;
import ru.yandex.practicum.common.dto.EventShortDto;
import ru.yandex.practicum.common.dto.UserDto;
import ru.yandex.practicum.common.dto.UserShortDto;
import ru.yandex.practicum.common.entity.Category;
import ru.yandex.practicum.common.entity.Event;
import ru.yandex.practicum.common.entity.User;
import ru.yandex.practicum.common.enums.AdminStateAction;
import ru.yandex.practicum.common.enums.EventState;
import ru.yandex.practicum.common.enums.SortType;
import ru.yandex.practicum.common.exception.BadRequestException;
import ru.yandex.practicum.common.exception.ConflictException;
import ru.yandex.practicum.common.exception.NotFoundException;
import ru.yandex.practicum.common.exception.ValidationException;
import ru.yandex.practicum.common.openfeign.ParticipationRequestFeignClient;
import ru.yandex.practicum.common.openfeign.StatisticsClient;
import ru.yandex.practicum.common.openfeign.UserFeignClient;
import ru.yandex.practicum.dto.EndpointHit;
import ru.yandex.practicum.dto.ViewStats;
import ru.yandex.practicum.event.management.categories.repository.CategoryRepository;
import ru.yandex.practicum.event.management.events.dto.NewEventDto;
import ru.yandex.practicum.event.management.events.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.event.management.events.dto.UpdateEventUserRequest;
import ru.yandex.practicum.event.management.events.mapper.EventMapper;
import ru.yandex.practicum.event.management.events.repository.EventRepository;
import ru.yandex.practicum.event.management.events.repository.EventRepositoryCustom;
import ru.yandex.practicum.event.management.events.utils.EventUpdateUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.common.config.JacksonConfig.DATE_TIME_FORMAT;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository repository;
    private final EventMapper mapper;
    private final StatisticsClient statsClient;
    private final UserFeignClient userClient;
    private final ParticipationRequestFeignClient participationRequestClient;
    private final CategoryRepository categoryRepository;
    private final EventRepositoryCustom eventRepositoryCustom;

    private static final String APP_NAME = "ewm-service";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);


    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        boolean findWithConfirmedRequests = false;

        if (users != null && users.contains(0L)) {
            findWithConfirmedRequests = true;
            users = null;
        }

        if (categories != null && categories.contains(0L)) {
            categories = null;
        }

        log.info("getAdminEvents: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        Page<Event> page = eventRepositoryCustom.findAllByAdminFilters(
                users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                pageable
        );

        if (page == null || page.getContent() == null || page.getContent().isEmpty()) {
            return List.of();
        }

        List<Event> events = page.getContent();
        if (findWithConfirmedRequests) {
            events = events.stream()
                    .filter(event -> getConfirmedRequestsCount(event) > 0)
                    .collect(Collectors.toList());
        }

        return events.stream()
                .map(this::enrichWithViewsAndRequests)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request) {
        log.info("Updating event by admin: eventId={}, request={}", eventId, request);

        Event event = getEventEntity(eventId);

        if (request.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Cannot publish the event because it's not in PENDING state");
            }

            if (request.getEventDate() != null &&
                    request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour from now");
            }
        }

        if (request.getStateAction() == AdminStateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Cannot reject published event");
            }
        }

        if (request.getParticipantLimit() != null && request.getParticipantLimit() < 0) {
            throw new BadRequestException("Participant limit must be positive or zero");
        }

        Category category = null;
        if (request.getCategory() != null) {
            category = getCategoryEntity(request.getCategory());
        }

        EventUpdateUtils.updateFromAdmin(event, request, category);

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        event = repository.save(event);
        return enrichWithViewsAndRequests(event);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        log.info("Getting events for user: userId={}", userId);

        getUserEntity(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> page = repository.findByInitiatorId(userId, pageable);

        return page.getContent().stream()
                .map(mapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        log.info("Creating event for user: userId={}, dto={}", userId, dto);

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ValidationException("description must not be blank");
        }

        if (dto.getAnnotation() == null || dto.getAnnotation().isBlank()) {
            throw new ValidationException("annotation must not be blank");
        }

        if (dto.getParticipantLimit() != null && dto.getParticipantLimit() < 0) {
            throw new BadRequestException("Participant limit cannot be negative");
        }

        User user = getUserEntity(userId);

        Category category = getCategoryEntity(dto.getCategory());

        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours from now");
        }

        Event event = mapper.toEntity(dto, category, user.getId());
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);

        event = repository.save(event);
        log.info("Created event with id: {}", event.getId());

        return mapper.toFullDto(event);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        log.info("Getting event for user: userId={}, eventId={}", userId, eventId);

        getUserEntity(userId);

        Event event = getEventEntity(eventId);

        if (event.getParticipantLimit() < 0) {
            throw new BadRequestException("Participant limit not be negative");
        }

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id " + eventId + " not found for user " + userId);
        }

        return mapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        log.info("Updating event by user: userId={}, eventId={}, request={}", userId, eventId, request);

        getUserEntity(userId);

        Event event = getEventEntity(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id " + eventId + " not found for user " + userId);
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (request.getEventDate() != null &&
                request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours from now");
        }

        if (request.getParticipantLimit() != null && request.getParticipantLimit() < 0) {
            throw new BadRequestException("Participant Limit must be positive or zero");
        }

        Category category = null;

        if (request.getCategory() != null) {
            category = getCategoryEntity(request.getCategory());
        }

        EventUpdateUtils.updateFromUser(event, request, category);

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                default -> throw new BadRequestException("Unknown state action: " + request.getStateAction());
            }
        }

        event = repository.save(event);
        log.info("Updated event with id: {}", eventId);

        return mapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        log.info("Getting public events with filters: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, " +
                        "onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        if (rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Range end do not to be after range start");
        }

        if (text == null) {
            text = "";
        }

        Pageable pageable;
        if (sort != null && sort.equalsIgnoreCase(SortType.VIEWS.name())) {
            pageable = PageRequest.of(from / size, size);
        } else {
            pageable = PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
        }

        Page<Event> page = eventRepositoryCustom.findPublishedEvents(text, categories, paid, rangeStart, rangeEnd, pageable);
        List<Event> events = page.getContent();

        if (onlyAvailable != null && onlyAvailable) {
            events = events.stream()
                    .filter(this::isEventAvailable)
                    .collect(Collectors.toList());
        }

        if (sort != null && sort.equalsIgnoreCase(SortType.VIEWS.name())) {
            Map<Long, Long> viewsMap = getViewsCountForEvents(events);
            events.sort(Comparator.comparingLong(e -> viewsMap.getOrDefault(e.getId(), 0L)));
        }

        List<EventShortDto> dtos = events.stream()
                .map(mapper::toShortDto)
                .collect(Collectors.toList());

        List<Event> finalEvents = events;
        dtos.forEach(dto -> {
            Event event = finalEvents.get(dtos.indexOf(dto));
            enrichShortDtoWithViewsAndRequests(dto, event);
        });

        EndpointHit hit = EndpointHit.builder()
                .app(APP_NAME)
                .uri("/events")
                .ip("0.0.0.0")
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.sendHit(hit);

        return dtos;
    }

    @Override
    public List<Event> getEventsByIds(List<Long> ids) {
        return repository.findAllById(ids);
    }

    @Override
    public boolean existsByInitiatorId(Long id) {
        return repository.existsByInitiatorId(id);
    }

    @Override
    public EventFullDto getEventFullDto(Long eventId) {
        Event event = getEventEntity(eventId);
        EventFullDto dto = mapper.toFullDto(event);
        dto.setInitiator(UserShortDto.builder()
                .id(event.getInitiatorId())
                .build());
        return dto;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId) {
        log.info("Getting public event: eventId={}", eventId);

        Event event = getEventEntity(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id " + eventId + " not found");
        }

        EndpointHit hit = EndpointHit.builder()
                .app(APP_NAME)
                .uri("/events/" + eventId)
                .ip("0.0.0.0")
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.sendHit(hit);

        return enrichWithViewsAndRequests(event);
    }

    @Override
    public Event getEventEntity(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));
    }

    @Override
    public boolean existsById(Long eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public List<Event> findAllByIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(eventIds);
    }

    @Override
    public boolean existsByCategoryId(Long id) {
        return repository.existsByCategoryId(id);
    }

    private EventFullDto enrichWithViewsAndRequests(Event event) {
        if (event == null) {
            return null;
        }
        EventFullDto dto = mapper.toFullDto(event);
        dto.setConfirmedRequests(getConfirmedRequestsCount(event));
        dto.setViews(getViewsCount(event));
        return dto;
    }

    private void enrichShortDtoWithViewsAndRequests(EventShortDto dto, Event event) {
        dto.setConfirmedRequests(getConfirmedRequestsCount(event));
        dto.setViews(getViewsCount(event));
    }

    private long getConfirmedRequestsCount(Event event) {
        if (event == null || event.getId() == null) {
            return 0L;
        }

        return participationRequestClient.getConfirmedCount(event.getId());
    }

    private long getViewsCount(Event event) {
        try {
            if (event == null || event.getId() == null) {
                return 0L;
            }

            LocalDateTime start = event.getPublishedOn() != null
                    ? event.getPublishedOn()
                    : event.getCreatedOn();

            if (start == null) {
                start = LocalDateTime.now().minusDays(1);
            }


            List<ViewStats> stats = statsClient.getStats(
                    start.format(FORMATTER),
                    LocalDateTime.now().format(FORMATTER),
                    "/events/" + event.getId(),
                    true
            );

            return stats.isEmpty() ? 0 : stats.getFirst().getHits();
        } catch (Exception e) {
            log.warn("Failed to get views for event {}", event.getId(), e);
            return 0;
        }
    }

    private Map<Long, Long> getViewsCountForEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Event> validEvents = events.stream()
                .filter(e -> e != null && e.getId() != null)
                .toList();

        if (validEvents.isEmpty()) {
            return Collections.emptyMap();
        }

        String uris = validEvents.stream()
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.joining(","));

        LocalDateTime start = validEvents.stream()
                .map(e -> e.getPublishedOn() != null ? e.getPublishedOn() : e.getCreatedOn())
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusDays(1));

        List<ViewStats> stats;
        try {
            stats = statsClient.getStats(
                    start.format(FORMATTER),
                    LocalDateTime.now().format(FORMATTER),
                    uris,
                    true
            );
        } catch (Exception e) {
            log.warn("Failed to get views for events", e);
            return Collections.emptyMap();
        }

        Map<Long, Long> result = new HashMap<>();
        for (ViewStats stat : stats) {
            String uri = stat.getUri();
            if (uri != null && uri.startsWith("/events/")) {
                try {
                    Long eventId = Long.parseLong(uri.substring("/events/".length()));
                    result.put(eventId, stat.getHits());
                } catch (NumberFormatException ignored) {
                    // пропускаем невалидные uri
                }
            }
        }
        return result;
    }

    private User getUserEntity(Long userId) {
        UserDto user = userClient.getEntity(userId);
        return new User(user.getId(), user.getEmail(), user.getName());
    }

    private Category getCategoryEntity(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(
                        "Category with id " + categoryId + " was not found"));
    }

    private boolean isEventAvailable(Event event) {
        if (event.getParticipantLimit() == 0) {
            return true;
        }
        return getConfirmedRequestsCount(event) < event.getParticipantLimit();
    }
}