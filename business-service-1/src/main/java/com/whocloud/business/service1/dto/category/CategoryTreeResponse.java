package com.whocloud.business.service1.dto.category;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Response for hierarchical category tree structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeResponse {
    
    private Long id;
    private String name;
    private Map<String, String> translations;
    private List<CategoryTreeResponse> children;
}
