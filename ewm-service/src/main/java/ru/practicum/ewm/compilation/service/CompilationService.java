package ru.practicum.ewm.compilation.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.model.CompilationDto;
import ru.practicum.ewm.compilation.model.NewCompilationDto;
import ru.practicum.ewm.compilation.model.UpdateCompilationRequest;

import java.util.List;

@Transactional(readOnly = true)
public interface CompilationService {
    @Transactional
    CompilationDto createCompilation(NewCompilationDto compilationDto);

    @Transactional
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request);

    @Transactional
    void deleteCompilationById(Long compId);

    CompilationDto getCompilationById(Long compId);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);
}
