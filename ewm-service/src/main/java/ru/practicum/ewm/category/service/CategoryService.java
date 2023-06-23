package ru.practicum.ewm.category.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.CategoryDto;
import ru.practicum.ewm.category.model.NewCategoryDto;

import java.util.List;

@Transactional(readOnly = true)
public interface CategoryService {
    @Transactional
    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    @Transactional
    CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long id);

    @Transactional
    void deleteCategoryById(Long id);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategoryById(Long id);

    Category getById(Long id);
}
