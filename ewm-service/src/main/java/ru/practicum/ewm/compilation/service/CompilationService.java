package ru.practicum.ewm.compilation.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.model.CompilationDto;
import ru.practicum.ewm.compilation.model.NewCompilationDto;
import ru.practicum.ewm.compilation.model.UpdateCompilationRequest;

@Transactional
public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto compilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request);

    void deleteCompilationById(Long compId);
}
