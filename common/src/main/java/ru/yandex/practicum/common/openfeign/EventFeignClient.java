package ru.yandex.practicum.common.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.common.dto.EventFullDto;
import ru.yandex.practicum.common.entity.Event;

import java.util.List;

@FeignClient(name = "event-management", contextId = "eventFeignClient")
public interface EventFeignClient {

    @GetMapping("/internal/events/{id}")
    EventFullDto getEvent(@PathVariable("id") Long id);

    @GetMapping("/internal/events/{id}")
    Event findById(@PathVariable("id") Long id);

    @GetMapping("/internal/events/exists/category/{id}")
    boolean existsByCategoryId(@PathVariable("id") Long id);

    @GetMapping("/internal/events")
    List<Event> findAllById(Iterable<Long> ids);

    @GetMapping("/internal/events/{id}/exists")
    boolean existsById(@PathVariable("id") Long id);

    @GetMapping("/internal/events/initiator/{id}")
    boolean existsByInitiatorId(@PathVariable("id") Long id);
}
