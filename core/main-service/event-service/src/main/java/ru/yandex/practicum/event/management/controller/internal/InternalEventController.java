package ru.yandex.practicum.event.management.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.common.dto.EventFullDto;
import ru.yandex.practicum.common.entity.Event;
import ru.yandex.practicum.event.management.events.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalEventController {

    private final EventService eventService;

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id) {
        return eventService.getEventFullDto(id);
    }

    @GetMapping("/{id}/exists")
    public boolean existsById(@PathVariable Long id) {
        return eventService.existsById(id);
    }

    @GetMapping("/exists/category/{id}")
    public boolean existsByCategoryId(@PathVariable Long id) {
        return eventService.existsByCategoryId(id);
    }

    @GetMapping()
    public List<Event> getEventsByIds(List<Long> ids) {
        return eventService.getEventsByIds(ids);
    }

    @GetMapping("/initiator/{id}")
    public boolean existsByInitiatorId(@PathVariable Long id) {
        return eventService.existsByInitiatorId(id);
    }
}
