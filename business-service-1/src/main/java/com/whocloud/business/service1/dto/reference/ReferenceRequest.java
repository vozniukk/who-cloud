package com.whocloud.business.service1.dto.reference;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private Long parentId; // For categories only

    private Map<String, String> translations;
}
