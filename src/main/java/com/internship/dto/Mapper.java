package com.internship.dto;

import com.internship.dto.Dtos.*;
import com.internship.model.*;
import com.internship.util.Dates;

public final class Mapper {
    private Mapper() {}

    public static UserDto user(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getStatus(), u.getSkills());
    }

    public static ListingDto listing(InternshipListing l) {
        return new ListingDto(l.getId(), l.getTitle(), l.getDescription(), l.getCompany().getId(),
                l.getCompany().getName(), l.getRequirements(), l.getStipend(), l.getDuration(),
                Dates.format(l.getDeadline()), l.getStatus());
    }

    public static ApplicationDto application(Application a) {
        return new ApplicationDto(a.getId(), a.getListing().getId(), a.getListing().getTitle(),
                a.getListing().getCompany().getName(), a.getIntern().getId(), a.getIntern().getName(),
                a.getIntern().getEmail(), a.getIntern().getSkills(), a.getStatus(), Dates.format(a.getAppliedDate()),
                a.getResumeUrl(), a.getFeedback(), a.getRating(), a.isCompleted());
    }

    public static LogDto log(DailyLog d) {
        return new LogDto(d.getId(), d.getApplication().getId(), d.getApplication().getIntern().getName(),
                d.getApplication().getListing().getTitle(), Dates.format(d.getDate()), d.getTasksCompleted(),
                d.getStatus(), d.getMentorNotes());
    }
}
