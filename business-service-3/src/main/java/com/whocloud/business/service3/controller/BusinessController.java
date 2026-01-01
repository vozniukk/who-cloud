package com.whocloud.business.service3.controller;

import com.whocloud.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business/service3")
public class BusinessController {

    @GetMapping
    public ApiResponse<String> getBusinessData() {
        return ApiResponse.success("Business data from Service 3");
    }
}
