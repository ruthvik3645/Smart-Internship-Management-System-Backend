package com.internship.service;

import com.internship.dto.Dtos.*;
import com.internship.dto.Mapper;
import com.internship.model.Application;
import com.internship.model.InternshipListing;
import com.internship.model.User;
import com.internship.repository.ApplicationRepository;
import com.internship.repository.InternshipListingRepository;
import com.internship.repository.UserRepository;
import com.internship.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository apps;
    private final InternshipListingRepository listings;
    private final UserRepository users;
    private final ReportService reports;

    @Transactional
    public ApplicationDto apply(ApplyRequest r, User student) {
        if (r.listingId() == null) throw ApiException.bad("Choose an internship.");
        if (r.resumeUrl() == null || !r.resumeUrl().trim().matches("(?i)^https?://\\S+$"))
            throw ApiException.bad("Add a resume link that starts with http:// or https://.");
        InternshipListing l = listings.findById(r.listingId())
                .orElseThrow(() -> ApiException.notFound("Internship not found."));
        if (l.getStatus() != InternshipListing.Status.OPEN)
            throw ApiException.conflict("This internship is no longer accepting applications.");
        if (l.getDeadline() != null && l.getDeadline().isBefore(LocalDate.now()))
            throw ApiException.conflict("The application deadline has passed.");
        if (apps.existsByIntern_IdAndListing_Id(student.getId(), l.getId()))
            throw ApiException.conflict("You have already applied to this internship.");

        Application a = new Application();
        a.setIntern(users.findById(student.getId()).orElseThrow());
        a.setListing(l);
        a.setAppliedDate(LocalDate.now());
        a.setResumeUrl(r.resumeUrl().trim());
        return Mapper.application(apps.save(a));
    }

    public List<ApplicationDto> mine(User student) {
        return apps.findByIntern_IdOrderByAppliedDateDesc(student.getId()).stream().map(Mapper::application).toList();
    }

    public List<ApplicationDto> forCompany(User company) {
        return apps.findByListing_Company_IdOrderByAppliedDateDesc(company.getId()).stream().map(Mapper::application).toList();
    }

    @Transactional
    public ApplicationDto updateStatus(Long id, StatusUpdate r, User company) {
        Application a = ownedByCompany(id, company);
        if (r.status() == null) throw ApiException.bad("Choose a status.");
        if (a.isCompleted()) throw ApiException.conflict("This internship is already complete.");
        a.setStatus(r.status());
        appendFeedback(a, r.feedback());
        return Mapper.application(apps.save(a));
    }

    @Transactional
    public ApplicationDto evaluate(Long id, EvaluationRequest r, User company) {
        Application a = ownedByCompany(id, company);
        if (a.getStatus() != Application.Status.ACCEPTED)
            throw ApiException.bad("Only accepted interns can be evaluated.");
        if (r.rating() != null && (r.rating() < 1 || r.rating() > 5))
            throw ApiException.bad("Rating must be between 1 and 5.");
        if (r.rating() != null) a.setRating(r.rating());
        if (r.completed()) {
            if (a.getRating() == null) throw ApiException.bad("Assign a rating before completing the internship.");
            a.setCompleted(true);
            a.setCompletedOn(LocalDate.now());
        }
        appendFeedback(a, r.feedback());
        return Mapper.application(apps.save(a));
    }

    public byte[] certificate(Long id, User student) {
        Application a = apps.findById(id).orElseThrow(() -> ApiException.notFound("Application not found."));
        if (!a.getIntern().getId().equals(student.getId()))
            throw ApiException.forbidden("This application belongs to another student.");
        if (!a.isCompleted())
            throw ApiException.conflict("Your certificate is available once the company marks the internship complete.");
        return reports.certificate(a);
    }

    private Application ownedByCompany(Long id, User company) {
        Application a = apps.findById(id).orElseThrow(() -> ApiException.notFound("Application not found."));
        if (!a.getListing().getCompany().getId().equals(company.getId()))
            throw ApiException.forbidden("This application belongs to another company.");
        return a;
    }

    /** Feedback is kept as a dated history so weekly and monthly notes accumulate. */
    private void appendFeedback(Application a, String text) {
        if (text == null || text.isBlank()) return;
        String line = LocalDate.now() + ": " + text.trim();
        a.setFeedback(a.getFeedback() == null || a.getFeedback().isBlank() ? line : a.getFeedback() + "\n" + line);
    }
}
