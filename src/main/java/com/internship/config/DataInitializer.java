package com.internship.config;

import com.internship.model.*;
import com.internship.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

/** Seeds demo accounts and sample data into an empty database. Disable with app.seed-demo-data=false. */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final InternshipListingRepository listings;
    private final ApplicationRepository apps;
    private final DailyLogRepository logs;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;

        user("System Admin", "admin@system.com", "admin123", User.Role.ADMIN, User.Status.ACTIVE, null);
        User company = user("TechCorp", "hr@techcorp.com", "company123", User.Role.COMPANY, User.Status.ACTIVE, null);
        User student = user("Asha Reddy", "student@univ.edu", "student123", User.Role.STUDENT, User.Status.ACTIVE,
                "Java, Spring Boot, React, SQL");
        user("Startup Labs", "pending@startuplabs.io", "company123", User.Role.COMPANY, User.Status.PENDING, null);

        InternshipListing backend = listing(company, "Backend Engineering Intern",
                "Build and test REST services with the payments platform team.",
                "Java or Kotlin, SQL basics, Git", 20000, "12 weeks", 30);
        listing(company, "Frontend Developer Intern",
                "Ship accessible UI components for the customer dashboard.",
                "React, HTML/CSS, comfort with code review", 15000, "10 weeks", 14);
        listing(company, "Data Analyst Intern",
                "Turn product usage data into weekly reports for the growth team.",
                "SQL, spreadsheets, Python or R", 12000, "8 weeks", 7);

        Application a = new Application();
        a.setIntern(student);
        a.setListing(backend);
        a.setStatus(Application.Status.ACCEPTED);
        a.setAppliedDate(LocalDate.now().minusDays(20));
        a.setResumeUrl("https://example.com/asha-reddy-resume");
        apps.save(a);

        log(a, LocalDate.now().minusDays(2), "Wrote integration tests for the payments client.",
                DailyLog.Status.APPROVED, "Good coverage. Add a test for the timeout path next.");
        log(a, LocalDate.now().minusDays(1), "Fixed a pagination bug in the orders endpoint.",
                DailyLog.Status.PENDING, null);
    }

    private User user(String name, String email, String pw, User.Role role, User.Status status, String skills) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(encoder.encode(pw));
        u.setRole(role);
        u.setStatus(status);
        u.setSkills(skills);
        return users.save(u);
    }

    private InternshipListing listing(User company, String title, String desc, String req, int stipend,
                                      String duration, int deadlineInDays) {
        InternshipListing l = new InternshipListing();
        l.setCompany(company);
        l.setTitle(title);
        l.setDescription(desc);
        l.setRequirements(req);
        l.setStipend(stipend);
        l.setDuration(duration);
        l.setDeadline(LocalDate.now().plusDays(deadlineInDays));
        return listings.save(l);
    }

    private void log(Application a, LocalDate date, String tasks, DailyLog.Status status, String notes) {
        DailyLog d = new DailyLog();
        d.setApplication(a);
        d.setDate(date);
        d.setTasksCompleted(tasks);
        d.setStatus(status);
        d.setMentorNotes(notes);
        logs.save(d);
    }
}
