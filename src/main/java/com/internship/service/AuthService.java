package com.internship.service;

import com.internship.dto.Dtos.*;
import com.internship.dto.Mapper;
import com.internship.model.User;
import com.internship.repository.UserRepository;
import com.internship.security.JwtUtil;
import com.internship.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthResponse register(RegisterRequest r) {
        if (blank(r.name()) || blank(r.email()) || blank(r.password()))
            throw ApiException.bad("Name, email and password are required.");
        String email = r.email().trim().toLowerCase();
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw ApiException.bad("Enter a valid email address.");
        if (r.password().length() < 6) throw ApiException.bad("Password must be at least 6 characters.");
        if (r.role() != User.Role.STUDENT && r.role() != User.Role.COMPANY)
            throw ApiException.bad("Choose Student or Company.");
        if (users.existsByEmail(email)) throw ApiException.conflict("An account with this email already exists.");

        User u = new User();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(r.password()));
        u.setRole(r.role());
        u.setSkills(blank(r.skills()) ? null : r.skills().trim());
        // Company accounts wait for admin approval; students can sign in straight away.
        u.setStatus(r.role() == User.Role.COMPANY ? User.Status.PENDING : User.Status.ACTIVE);
        users.save(u);

        if (u.getStatus() == User.Status.PENDING)
            return new AuthResponse(null, Mapper.user(u),
                    "Account created. An administrator needs to approve your company before you can sign in.");
        return new AuthResponse(jwt.generateToken(u), Mapper.user(u), null);
    }

    public AuthResponse login(LoginRequest r) {
        if (blank(r.email()) || blank(r.password())) throw ApiException.bad("Enter your email and password.");
        User u = users.findByEmail(r.email().trim().toLowerCase())
                .filter(x -> encoder.matches(r.password(), x.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        if (u.getStatus() == User.Status.PENDING)
            throw ApiException.forbidden("Your company account is waiting for administrator approval.");
        if (u.getStatus() == User.Status.LOCKED)
            throw ApiException.forbidden("This account is locked. Contact an administrator.");
        return new AuthResponse(jwt.generateToken(u), Mapper.user(u), null);
    }

    static boolean blank(String s) { return s == null || s.isBlank(); }
}
