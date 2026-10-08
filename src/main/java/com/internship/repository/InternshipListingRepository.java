package com.internship.repository;

import com.internship.model.InternshipListing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InternshipListingRepository extends JpaRepository<InternshipListing, Long> {
    List<InternshipListing> findByStatusOrderByCreatedAtDesc(InternshipListing.Status status);
    List<InternshipListing> findByCompany_IdOrderByCreatedAtDesc(Long companyId);
    long countByStatus(InternshipListing.Status status);
}
