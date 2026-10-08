package com.internship.service;

import com.internship.dto.Dtos.*;
import com.internship.dto.Mapper;
import com.internship.model.Application;
import com.internship.model.DailyLog;
import com.internship.model.User;
import com.internship.repository.ApplicationRepository;
import com.internship.repository.DailyLogRepository;
import com.internship.util.Dates;
import com.internship.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyLogService {
    private final DailyLogRepository logs;
    private final ApplicationRepository apps;

    @Transactional
    public LogDto submit(LogRequest r, User student) {
        Application a = apps.findById(r.applicationId() == null ? -1L : r.applicationId())
                .orElseThrow(() -> ApiException.notFound("Application not found."));
        if (!a.getIntern().getId().equals(student.getId()))
            throw ApiException.forbidden("This application belongs to another student.");
        if (a.getStatus() != Application.Status.ACCEPTED || a.isCompleted())
            throw ApiException.conflict("You can only log days for an active internship.");
        LocalDate date = Dates.parse(r.date());
        if (date == null) date = LocalDate.now();
        if (date.isAfter(LocalDate.now())) throw ApiException.bad("You can't log a future date.");
        if (r.tasksCompleted() == null || r.tasksCompleted().isBlank())
            throw ApiException.bad("Describe the tasks you completed.");
        if (logs.existsByApplication_IdAndDate(a.getId(), date))
            throw ApiException.conflict("You already submitted a log for that date.");

        DailyLog d = new DailyLog();
        d.setApplication(a);
        d.setDate(date);
        d.setTasksCompleted(r.tasksCompleted().trim());
        return Mapper.log(logs.save(d));
    }

    @Transactional
    public LogDto resubmit(Long id, LogRequest r, User student) {
        DailyLog d = find(id);
        if (!d.getApplication().getIntern().getId().equals(student.getId()))
            throw ApiException.forbidden("This log belongs to another student.");
        if (d.getStatus() == DailyLog.Status.APPROVED) throw ApiException.conflict("Approved logs can't be edited.");
        if (r.tasksCompleted() == null || r.tasksCompleted().isBlank())
            throw ApiException.bad("Describe the tasks you completed.");
        d.setTasksCompleted(r.tasksCompleted().trim());
        d.setStatus(DailyLog.Status.PENDING);
        return Mapper.log(logs.save(d));
    }

    public List<LogDto> mine(User student) {
        return logs.findByApplication_Intern_IdOrderByDateDesc(student.getId()).stream().map(Mapper::log).toList();
    }

    public List<LogDto> forReview(User company) {
        return logs.findByApplication_Listing_Company_IdOrderByDateDesc(company.getId()).stream().map(Mapper::log).toList();
    }

    @Transactional
    public LogDto review(Long id, LogReview r, User company) {
        DailyLog d = find(id);
        if (!d.getApplication().getListing().getCompany().getId().equals(company.getId()))
            throw ApiException.forbidden("This log belongs to another company's intern.");
        if (r.status() != DailyLog.Status.APPROVED && r.status() != DailyLog.Status.REJECTED)
            throw ApiException.bad("Choose Approved or Rejected.");
        d.setStatus(r.status());
        d.setMentorNotes(r.mentorNotes() == null || r.mentorNotes().isBlank() ? null : r.mentorNotes().trim());
        return Mapper.log(logs.save(d));
    }

    private DailyLog find(Long id) {
        return logs.findById(id).orElseThrow(() -> ApiException.notFound("Log not found."));
    }
}
