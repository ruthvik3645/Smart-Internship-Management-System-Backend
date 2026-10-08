package com.internship.web;

import com.internship.dto.Dtos.*;
import com.internship.security.CurrentUser;
import com.internship.service.InternshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/internships")
@RequiredArgsConstructor
public class InternshipController {
    private final InternshipService service;

    @GetMapping
    public List<ListingDto> open() { return service.open(); }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('COMPANY')")
    public List<ListingDto> mine() { return service.mine(CurrentUser.get()); }

    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ListingDto create(@RequestBody ListingRequest r) { return service.create(r, CurrentUser.get()); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ListingDto update(@PathVariable Long id, @RequestBody ListingRequest r) {
        return service.update(id, r, CurrentUser.get());
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('COMPANY')")
    public ListingDto archive(@PathVariable Long id) { return service.archive(id, CurrentUser.get()); }
}
