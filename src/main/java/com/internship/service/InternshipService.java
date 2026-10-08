package com.internship.service;

import com.internship.dto.Dtos.*;
import com.internship.dto.Mapper;
import com.internship.model.InternshipListing;
import com.internship.model.User;
import com.internship.repository.InternshipListingRepository;
import com.internship.repository.UserRepository;
import com.internship.util.Dates;
import com.internship.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InternshipService {
    private final InternshipListingRepository repo;
    private final UserRepository users;

    public List<ListingDto> open() {
        return repo.findByStatusOrderByCreatedAtDesc(InternshipListing.Status.OPEN).stream().map(Mapper::listing).toList();
    }

    public List<ListingDto> mine(User company) {
        return repo.findByCompany_IdOrderByCreatedAtDesc(company.getId()).stream().map(Mapper::listing).toList();
    }

    @Transactional
    public ListingDto create(ListingRequest r, User company) {
        InternshipListing l = new InternshipListing();
        l.setCompany(users.findById(company.getId()).orElseThrow());
        fill(l, r);
        return Mapper.listing(repo.save(l));
    }

    @Transactional
    public ListingDto update(Long id, ListingRequest r, User company) {
        InternshipListing l = owned(id, company);
        fill(l, r);
        return Mapper.listing(repo.save(l));
    }

    @Transactional
    public ListingDto archive(Long id, User company) {
        InternshipListing l = owned(id, company);
        l.setStatus(InternshipListing.Status.ARCHIVED);
        return Mapper.listing(repo.save(l));
    }

    private void fill(InternshipListing l, ListingRequest r) {
        if (r.title() == null || r.title().isBlank()) throw ApiException.bad("Add a title for the internship.");
        if (r.stipend() != null && r.stipend() < 0) throw ApiException.bad("Stipend can't be negative.");
        l.setTitle(r.title().trim());
        l.setDescription(r.description());
        l.setRequirements(r.requirements());
        l.setStipend(r.stipend());
        l.setDuration(r.duration());
        l.setDeadline(Dates.parse(r.deadline()));
    }

    private InternshipListing owned(Long id, User company) {
        InternshipListing l = repo.findById(id).orElseThrow(() -> ApiException.notFound("Posting not found."));
        if (!l.getCompany().getId().equals(company.getId()))
            throw ApiException.forbidden("This posting belongs to another company.");
        return l;
    }
}
