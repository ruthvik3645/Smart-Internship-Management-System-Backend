package com.internship.web;

import com.internship.dto.Dtos.StatsDto;
import com.internship.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final ReportService reports;

    @GetMapping("/stats")
    public StatsDto stats() { return reports.stats(); }

    @GetMapping("/reports")
    public ResponseEntity<byte[]> export(@RequestParam String type, @RequestParam String format) {
        ReportService.Export e = reports.export(type, format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + e.filename() + "\"")
                .contentType(MediaType.parseMediaType(e.contentType()))
                .body(e.data());
    }
}
