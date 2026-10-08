package com.internship.web;

import com.internship.dto.Dtos.*;
import com.internship.security.CurrentUser;
import com.internship.service.DailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class DailyLogController {
    private final DailyLogService service;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public LogDto submit(@RequestBody LogRequest r) { return service.submit(r, CurrentUser.get()); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public LogDto resubmit(@PathVariable Long id, @RequestBody LogRequest r) {
        return service.resubmit(id, r, CurrentUser.get());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public List<LogDto> mine() { return service.mine(CurrentUser.get()); }

    @GetMapping("/review")
    @PreAuthorize("hasRole('COMPANY')")
    public List<LogDto> forReview() { return service.forReview(CurrentUser.get()); }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('COMPANY')")
    public LogDto review(@PathVariable Long id, @RequestBody LogReview r) {
        return service.review(id, r, CurrentUser.get());
    }
}
