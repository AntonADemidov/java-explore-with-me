package ru.practicum.ewm.category.service;

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
import ru.practicum.ewm.util.PageNumber;
import ru.practicum.ewm.util.exception.category.CategoryNotFoundException;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.CategoryDto;
import ru.practicum.ewm.category.model.NewCategoryDto;
import ru.practicum.ewm.category.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CategoryServiceImpl implements CategoryService {
    CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.save(CategoryMapper.toCategory(newCategoryDto));
        CategoryDto categoryDto = CategoryMapper.toCategoryDto(category);
        log.info("Новая категория добавлена в базу: {} c id #{}.", categoryDto.getName(), categoryDto.getId());
        return categoryDto;
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long id) {
        Category category = getById(id);
        category.setName(newCategoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        log.info("В базе обновлена категория {} c id #{}.", updatedCategory.getName(), updatedCategory.getId());
        return CategoryMapper.toCategoryDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long id) {
        Category category = getById(id);
        categoryRepository.delete(category);
        log.info("Из базы удалена категория: {} c id #{}.", category.getName(), category.getId());
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable request = PageRequest.of(PageNumber.get(from, size), size);
        Page<Category> requestPage = categoryRepository.findAll(request);
        List<Category> categories = requestPage.getContent();

        List<CategoryDto> categoryDtos = categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());

        log.info("Список категорий по запросу: {}.", categoryDtos);
        return categoryDtos;
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        CategoryDto categoryDto = CategoryMapper.toCategoryDto(getById(id));
        log.info("Просмотр категории: {}.", categoryDto);
        return categoryDto;
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new CategoryNotFoundException(String.format("Категория с id #%d отсутствует в базе.", id)));
    }
}