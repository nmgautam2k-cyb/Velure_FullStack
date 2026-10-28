package com.ecommerce.controller;

import com.ecommerce.dto.SummaryResponse;
import com.ecommerce.service.AdminSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/summary")
public class AdminSummaryController {

    private final AdminSummaryService summaryService;

    public AdminSummaryController(AdminSummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping
    public SummaryResponse summary() {
        return summaryService.summary();
    }
}
