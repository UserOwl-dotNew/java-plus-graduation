package ru.yandex.practicum.common.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.common.config.StatisticsFeignConfig;
import ru.yandex.practicum.dto.EndpointHit;
import ru.yandex.practicum.dto.ViewStats;

import java.util.List;

@FeignClient(
        name = "stats-server",
        configuration = StatisticsFeignConfig.class
)
public interface StatisticsClient {

    @PostMapping("/hit")
    void sendHit(@RequestBody EndpointHit hit);

    @GetMapping("/stats")
    List<ViewStats> getStats(
            @RequestParam("start") String start,
            @RequestParam("end") String end,
            @RequestParam(value = "uris", required = false) String uris,
            @RequestParam("unique") Boolean unique
    );
}
