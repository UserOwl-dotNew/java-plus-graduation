package ru.yandex.practicum.event.management.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.common.dto.CompilationDto;
import ru.yandex.practicum.common.entity.Compilation;
import ru.yandex.practicum.common.entity.Event;
import ru.yandex.practicum.common.exception.ConflictException;
import ru.yandex.practicum.common.exception.NotFoundException;
import ru.yandex.practicum.event.management.compilations.dto.NewCompilationDto;
import ru.yandex.practicum.event.management.compilations.dto.UpdateCompilationRequest;
import ru.yandex.practicum.event.management.compilations.mapper.CompilationMapper;
import ru.yandex.practicum.event.management.compilations.repository.CompilationRepository;
import ru.yandex.practicum.event.management.events.repository.EventRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        log.info("Creating compilation: {}", dto);

        Set<Event> events = new LinkedHashSet<>();

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            List<Event> foundEvents = new ArrayList<>(eventRepository.findAllById(dto.getEvents()));

            Set<Long> foundEventIds = foundEvents.stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet());

            Set<Long> missingEventIds = new LinkedHashSet<>(dto.getEvents());
            missingEventIds.removeAll(foundEventIds);

            if (!missingEventIds.isEmpty()) {
                throw new NotFoundException(
                        "Events with ids " + missingEventIds + " were not found"
                );
            }

            foundEvents.sort(Comparator.comparing(Event::getId));
            events.addAll(foundEvents);
        }

        Compilation compilation = compilationMapper.toEntity(dto, events);

        try {
            Compilation savedCompilation = compilationRepository.saveAndFlush(compilation);
            return compilationMapper.toDto(savedCompilation);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    "Compilation with title '" + dto.getTitle() + "' already exists"
            );
        }
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(
            Long compId,
            UpdateCompilationRequest dto
    ) {
        log.info("Updating compilation with id {}: {}", compId, dto);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation with id=" + compId + " was not found"
                ));

        if (dto == null) {
            return compilationMapper.toDto(compilation);
        }

        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }

        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        if (dto.getEvents() != null) {
            Set<Event> events = new LinkedHashSet<>();

            if (!dto.getEvents().isEmpty()) {
                List<Event> foundEvents = new ArrayList<>(eventRepository.findAllById(dto.getEvents()));

                Set<Long> foundEventIds = foundEvents.stream()
                        .map(Event::getId)
                        .collect(Collectors.toSet());

                Set<Long> missingEventIds = new LinkedHashSet<>(dto.getEvents());
                missingEventIds.removeAll(foundEventIds);

                if (!missingEventIds.isEmpty()) {
                    throw new NotFoundException(
                            "Events with ids " + missingEventIds + " were not found"
                    );
                }

                foundEvents.sort(Comparator.comparing(Event::getId));
                events.addAll(foundEvents);
            }

            compilation.setEvents(events);
        }

        try {
            Compilation savedCompilation = compilationRepository.save(compilation);
            return compilationMapper.toDto(savedCompilation);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    "Compilation with title '" + compilation.getTitle() + "' already exists"
            );
        }
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Deleting compilation with id {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation with id=" + compId + " was not found"
                ));

        compilationRepository.delete(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        Page<Long> compilationIdsPage = compilationRepository.findCompilationIds(pinned, pageable);

        List<Long> compilationIds = compilationIdsPage.getContent();

        if (compilationIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Compilation> compilationsById = compilationRepository
                .findAllByIdsWithEvents(compilationIds)
                .stream()
                .collect(Collectors.toMap(
                        Compilation::getId,
                        compilation -> compilation
                ));

        return compilationIds.stream()
                .map(compilationsById::get)
                .filter(Objects::nonNull)
                .map(compilationMapper::toDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation with id=" + compId + " was not found"
                ));

        return compilationMapper.toDto(compilation);
    }
}