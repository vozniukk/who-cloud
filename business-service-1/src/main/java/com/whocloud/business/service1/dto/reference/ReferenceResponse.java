package com.whocloud.business.service1.dto.reference;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceResponse {
    
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private Map<String, String> translations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
