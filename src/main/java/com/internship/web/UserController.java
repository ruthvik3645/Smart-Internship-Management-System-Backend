package com.internship.web;

import com.internship.dto.Dtos.*;
import com.internship.dto.Mapper;
import com.internship.security.CurrentUser;
import com.internship.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/me")
    public UserDto me() { return Mapper.user(CurrentUser.get()); }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> all() { return service.all(); }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto approve(@PathVariable Long id) { return service.approve(id); }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto status(@PathVariable Long id, @RequestBody UserStatusRequest r) {
        return service.setStatus(id, r.status(), CurrentUser.get());
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto role(@PathVariable Long id, @RequestBody UserRoleRequest r) {
        return service.setRole(id, r.role(), CurrentUser.get());
    }
}
