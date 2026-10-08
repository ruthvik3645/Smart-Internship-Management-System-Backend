package com.internship.web;

import com.internship.dto.Dtos.*;
import com.internship.security.CurrentUser;
import com.internship.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService service;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ApplicationDto apply(@RequestBody ApplyRequest r) { return service.apply(r, CurrentUser.get()); }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ApplicationDto> mine() { return service.mine(CurrentUser.get()); }

    @GetMapping("/company")
    @PreAuthorize("hasRole('COMPANY')")
    public List<ApplicationDto> forCompany() { return service.forCompany(CurrentUser.get()); }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('COMPANY')")
    public ApplicationDto status(@PathVariable Long id, @RequestBody StatusUpdate r) {
        return service.updateStatus(id, r, CurrentUser.get());
    }

    @PatchMapping("/{id}/evaluation")
    @PreAuthorize("hasRole('COMPANY')")
    public ApplicationDto evaluate(@PathVariable Long id, @RequestBody EvaluationRequest r) {
        return service.evaluate(id, r, CurrentUser.get());
    }

    @GetMapping("/{id}/certificate")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<byte[]> certificate(@PathVariable Long id) {
        byte[] pdf = service.certificate(id, CurrentUser.get());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"internship-certificate.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
