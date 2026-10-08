package com.internship.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
public class InternshipListing {
    public enum Status { OPEN, ARCHIVED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String title;
    @Column(length = 4000) private String description;
    @ManyToOne(optional = false) private User company;
    @Column(length = 2000) private String requirements;
    private Integer stipend;
    private String duration;
    private LocalDate deadline;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status = Status.OPEN;
    private Instant createdAt = Instant.now();
}
