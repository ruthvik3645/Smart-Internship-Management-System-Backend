package com.internship.dto;

import com.internship.model.*;

public final class Dtos {
    private Dtos() {}

    public record RegisterRequest(String name, String email, String password, User.Role role, String skills) {}
    public record LoginRequest(String email, String password) {}
    public record UserDto(Long id, String name, String email, User.Role role, User.Status status, String skills) {}
    public record AuthResponse(String token, UserDto user, String message) {}

    public record ListingRequest(String title, String description, String requirements, Integer stipend, String duration, String deadline) {}
    public record ListingDto(Long id, String title, String description, Long companyId, String companyName,
                             String requirements, Integer stipend, String duration, String deadline,
                             InternshipListing.Status status) {}

    public record ApplyRequest(Long listingId, String resumeUrl) {}
    public record StatusUpdate(Application.Status status, String feedback) {}
    public record EvaluationRequest(Integer rating, String feedback, boolean completed) {}
    public record ApplicationDto(Long id, Long listingId, String listingTitle, String companyName,
                                 Long internId, String internName, String internEmail, String internSkills,
                                 Application.Status status, String appliedDate, String resumeUrl,
                                 String feedback, Integer rating, boolean completed) {}

    public record LogRequest(Long applicationId, String date, String tasksCompleted) {}
    public record LogReview(DailyLog.Status status, String mentorNotes) {}
    public record LogDto(Long id, Long applicationId, String internName, String listingTitle, String date,
                         String tasksCompleted, DailyLog.Status status, String mentorNotes) {}

    public record UserStatusRequest(User.Status status) {}
    public record UserRoleRequest(User.Role role) {}
    public record StatsDto(long totalInterns, long totalCompanies, long pendingCompanies, long activeListings,
                           long totalApplications, long acceptedInterns, long completedInterns, double completionRate) {}
}
