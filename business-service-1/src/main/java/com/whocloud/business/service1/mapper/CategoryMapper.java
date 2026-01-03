package com.whocloud.business.service1.mapper;

import com.whocloud.business.service1.dto.category.CategoryRequest;
import com.whocloud.business.service1.dto.category.CategoryResponse;
import com.whocloud.business.service1.dto.category.CategoryTreeResponse;
import com.whocloud.business.service1.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .translations(category.getTranslations())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public Category toEntity(CategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .translations(request.getTranslations())
                .build();
    }

    public void updateEntityFromRequest(Category category, CategoryRequest request) {
        category.setName(request.getName());
        category.setTranslations(request.getTranslations());
    }

    public CategoryTreeResponse toTreeResponse(Category category) {
        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .translations(category.getTranslations())
                .build();
    }
}
