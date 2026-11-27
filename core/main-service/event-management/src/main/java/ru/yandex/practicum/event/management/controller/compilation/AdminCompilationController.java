package ru.yandex.practicum.event.management.controller.compilation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.common.dto.CompilationDto;
import ru.yandex.practicum.event.management.compilations.dto.NewCompilationDto;
import ru.yandex.practicum.event.management.compilations.dto.UpdateCompilationRequest;
import ru.yandex.practicum.event.management.compilations.service.CompilationService;
import ru.yandex.practicum.event.management.controller.api.compilation.AdminCompilationControllerApi;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class AdminCompilationController implements AdminCompilationControllerApi {
    private final CompilationService compilationService;

    @Override
    public CompilationDto saveCompilation(@Valid @RequestBody NewCompilationDto dto) {
        log.info("POST /admin/compilations with request: {}", dto);
        return compilationService.createCompilation(dto);
    }

    @Override
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Valid @RequestBody(required = false) UpdateCompilationRequest dto) {
        log.info("PATCH /admin/compilations/{} with request: {}", compId, dto);
        return compilationService.updateCompilation(compId, dto);
    }

    @Override
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("DELETE /admin/compilations/{}", compId);
        compilationService.deleteCompilation(compId);
    }
}