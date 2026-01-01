package com.whocloud.information.b.controller;

import com.whocloud.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/information/b")
public class InformationController {

    @GetMapping
    public ApiResponse<String> getInformation() {
        return ApiResponse.success("Information from Service B");
    }
}
