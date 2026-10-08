package com.internship.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"application_id", "date"}))
@Getter @Setter @NoArgsConstructor
public class DailyLog {
    public enum Status { PENDING, APPROVED, REJECTED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private Application application;
    @Column(nullable = false) private LocalDate date;
    @Column(length = 2000) private String tasksCompleted;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status = Status.PENDING;
    @Column(length = 1000) private String mentorNotes;
}
