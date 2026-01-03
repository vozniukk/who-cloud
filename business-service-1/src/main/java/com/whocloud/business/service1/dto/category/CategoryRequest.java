package com.whocloud.business.service1.dto.category;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    private Long parentId; // Null for root categories

    @NotNull(message = "Translations are required")
    private Map<String, String> translations; // {"en": "Computers", "ru": "Компьютеры", "uk": "Комп'ютери"}
}
