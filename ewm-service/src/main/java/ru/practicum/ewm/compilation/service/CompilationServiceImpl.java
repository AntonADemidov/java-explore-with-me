package ru.practicum.ewm.compilation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.PageNumber;
import ru.practicum.ewm.compilation.exception.CompilationNotFoundException;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.CompilationDto;
import ru.practicum.ewm.compilation.model.NewCompilationDto;
import ru.practicum.ewm.compilation.model.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CompilationServiceImpl implements CompilationService {
    CompilationRepository compilationRepository;
    EventService eventService;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Set<Event> events = new HashSet<>();

        if (newCompilationDto.getEvents() != null) {
            for (Long id : newCompilationDto.getEvents()) {
                Event event = eventService.getEventById(id);
                events.add(event);
            }
        }

        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        compilation.setEvents(events);

        Compilation actualCompilation = compilationRepository.save(compilation);
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(actualCompilation);
        log.info("Новая подборка событий добавлена в базу: compId={}.", compilationDto.getId());
        return compilationDto;
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = getById(compId);

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
            compilation.getEvents().clear();

            for (Long eventId : request.getEvents()) {
                Event event = eventService.getEventById(eventId);
                compilation.getEvents().add(event);
            }
        }

        Compilation actualCompilation = compilationRepository.save(compilation);
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(actualCompilation);
        log.info("Подборка событий обновлена в базе: compId={}.", compilationDto.getId());
        return compilationDto;
    }

    @Override
    public void deleteCompilationById(Long compId) {
        Compilation compilation = getById(compId);
        compilationRepository.delete(compilation);
        log.info("Подборка удалена из базы: compId={}.", compId);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(getById(compId));
        log.info("Подборка найдена: compId={}.", compilationDto.getId());
        return compilationDto;
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable request = PageRequest.of(PageNumber.get(from, size), size);
        Page<Compilation> requestPage = compilationRepository.findAllByPinnedEquals(pinned, request);
        List<Compilation> compilations = requestPage.getContent();

        List<CompilationDto> compilationDtos = compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
        log.info("Список подборок событий сформирован: количество элементов={}.", compilationDtos.size());
        return compilationDtos;
    }

    private Compilation getById(Long id) {
        return compilationRepository.findById(id).orElseThrow(() ->
                new CompilationNotFoundException(String.format("Подборка с compId=%d отсутствует в базе.", id)));
    }
}