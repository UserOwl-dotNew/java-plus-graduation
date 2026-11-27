package ru.yandex.practicum.event.management.controller.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.common.dto.EventFullDto;
import ru.yandex.practicum.common.enums.EventState;
import ru.yandex.practicum.event.management.controller.api.event.AdminEventControllerApi;
import ru.yandex.practicum.event.management.events.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.event.management.events.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static ru.yandex.practicum.common.config.JacksonConfig.DATE_TIME_FORMAT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController implements AdminEventControllerApi {

    private final EventService service;

    @Override
    public Collection<EventFullDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("GET /admin/events with params: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return service.getAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @Override
    public EventFullDto updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest request
    ) {
        log.info("PATCH /admin/events/{} with request: {}", eventId, request);
        return service.updateAdminEvent(eventId, request);
    }
}
