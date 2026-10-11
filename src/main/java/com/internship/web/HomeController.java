package com.internship.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping({"/", "/health"})
    public String health() {
        return "Smart Internship Management System API is running";
    }
}
