package com.bookshop.pos.controller;

import com.bookshop.pos.dto.ReportSummary;
import com.bookshop.pos.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')") // the whole reports area is admin-only
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ReportSummary summary(@RequestParam(defaultValue = "today") String range) {
        return reportService.summary(range);
    }
}
