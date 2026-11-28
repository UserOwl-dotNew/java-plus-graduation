package ru.yandex.practicum.statistics.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.yandex.practicum.dto.EndpointHit;
import ru.yandex.practicum.statistics.server.model.HitEntity;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HitEntityMapper {
    HitEntity toEntity(EndpointHit endpointHit);
}
