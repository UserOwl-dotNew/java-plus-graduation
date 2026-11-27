package ru.yandex.practicum.management.participation.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.management.participation.requests.service.ParticipationRequestService;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class InternalParticipationController {

    private final ParticipationRequestService requestService;

    @GetMapping("/event/{eventId}/confirmed-count")
    public long getConfirmedCount(@PathVariable Long eventId) {
        return requestService.getConfirmedCount(eventId);
    }
}