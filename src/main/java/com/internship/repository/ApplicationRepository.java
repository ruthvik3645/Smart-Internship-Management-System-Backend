package com.internship.repository;

import com.internship.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByIntern_IdOrderByAppliedDateDesc(Long internId);
    List<Application> findByListing_Company_IdOrderByAppliedDateDesc(Long companyId);
    boolean existsByIntern_IdAndListing_Id(Long internId, Long listingId);
    long countByStatus(Application.Status status);
    long countByCompletedTrue();
}
