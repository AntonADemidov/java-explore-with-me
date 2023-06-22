package ru.practicum.ewm.category;

import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.model.CategoryDto;
import ru.practicum.ewm.category.model.NewCategoryDto;

public class CategoryMapper {
    public static Category toCategory(NewCategoryDto newCategoryDto) {
        return new Category(null, newCategoryDto.getName());
    }

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}