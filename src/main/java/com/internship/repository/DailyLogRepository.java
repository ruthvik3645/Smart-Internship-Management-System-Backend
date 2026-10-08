package com.internship.repository;

import com.internship.model.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
    List<DailyLog> findByApplication_Intern_IdOrderByDateDesc(Long internId);
    List<DailyLog> findByApplication_Listing_Company_IdOrderByDateDesc(Long companyId);
    boolean existsByApplication_IdAndDate(Long applicationId, LocalDate date);
}
