package com.internship.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"intern_id", "listing_id"}))
@Getter @Setter @NoArgsConstructor
public class Application {
    public enum Status { PENDING, SHORTLISTED, INTERVIEW_SCHEDULED, ACCEPTED, REJECTED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private User intern;
    @ManyToOne(optional = false) private InternshipListing listing;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status = Status.PENDING;
    private LocalDate appliedDate;
    @Column(length = 1000) private String resumeUrl;
    @Column(length = 8000) private String feedback;
    private Integer rating;
    private boolean completed;
    private LocalDate completedOn;
}
