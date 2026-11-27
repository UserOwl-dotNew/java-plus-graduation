package ru.yandex.practicum.statistics.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.EndpointHit;
import ru.yandex.practicum.dto.ViewStats;
import ru.yandex.practicum.statistics.server.mapper.HitEntityMapper;
import ru.yandex.practicum.statistics.server.model.HitEntity;
import ru.yandex.practicum.statistics.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository repository;
    private final HitEntityMapper mapper;

    @Override
    public HitEntity save(EndpointHit endpointHit) {
        HitEntity hit = mapper.toEntity(endpointHit);
        HitEntity saveHit = repository.save(hit);
        return saveHit;
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        return unique ? repository.findStatsUnique(start, end, uris) :
                repository.findStats(start, end, uris);
    }
}
