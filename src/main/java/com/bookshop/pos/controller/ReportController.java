package com.bookshop.pos.controller;

import com.bookshop.pos.dto.ReportSummary;
import com.bookshop.pos.dto.WeekReport;
import com.bookshop.pos.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

// the whole reports area is admin-only
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/today")
    public ReportSummary today() {
        return reportService.today();
    }

    @GetMapping("/week")
    public WeekReport week() {
        return reportService.thisWeek();
    }

    @GetMapping("/day")
    public ReportSummary day(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportService.day(date);
    }
}
