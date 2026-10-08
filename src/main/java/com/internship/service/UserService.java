package com.internship.service;

import com.internship.dto.Dtos.UserDto;
import com.internship.dto.Mapper;
import com.internship.model.User;
import com.internship.repository.UserRepository;
import com.internship.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository users;

    public List<UserDto> all() {
        return users.findAll(Sort.by("id")).stream().map(Mapper::user).toList();
    }

    @Transactional
    public UserDto approve(Long id) {
        User u = find(id);
        if (u.getStatus() != User.Status.PENDING) throw ApiException.bad("Only pending accounts can be approved.");
        u.setStatus(User.Status.ACTIVE);
        return Mapper.user(users.save(u));
    }

    @Transactional
    public UserDto setStatus(Long id, User.Status status, User actor) {
        if (status != User.Status.ACTIVE && status != User.Status.LOCKED)
            throw ApiException.bad("Status must be ACTIVE or LOCKED.");
        notSelf(id, actor);
        User u = find(id);
        u.setStatus(status);
        return Mapper.user(users.save(u));
    }

    @Transactional
    public UserDto setRole(Long id, User.Role role, User actor) {
        if (role == null) throw ApiException.bad("Choose a role.");
        notSelf(id, actor);
        User u = find(id);
        u.setRole(role);
        return Mapper.user(users.save(u));
    }

    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("User not found."));
    }

    private void notSelf(Long id, User actor) {
        if (actor.getId().equals(id)) throw ApiException.bad("You can't change your own account.");
    }
}
