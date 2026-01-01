package com.whocloud.information.a.controller;

import com.whocloud.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/information/a")
public class InformationController {

    @GetMapping
    public ApiResponse<String> getInformation() {
        return ApiResponse.success("Information from Service A");
    }
}
