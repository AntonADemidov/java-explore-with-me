package ru.practicum.ewm.compilation.mapper;

import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.CompilationDto;
import ru.practicum.ewm.compilation.model.NewCompilationDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CompilationMapper {

    public static Compilation toCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(newCompilationDto.getPinned());
        return compilation;
    }

    public static CompilationDto toCompilationDto(Compilation actualCompilation) {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(actualCompilation.getId());
        compilationDto.setTitle(actualCompilation.getTitle());
        compilationDto.setPinned(actualCompilation.getPinned());
        compilationDto.setEvents(actualCompilation.getEvents().stream().map(EventMapper::toEventShortDto).collect(Collectors.toList()));
        return compilationDto;
    }
}