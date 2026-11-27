package ru.yandex.practicum.common.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "management-participation", contextId = "participationRequestFeignClient")
public interface ParticipationRequestFeignClient {

    @GetMapping("/internal/requests/event/{eventId}/confirmed-count")
    long getConfirmedCount(@PathVariable("eventId") Long eventId);
}
