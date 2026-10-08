package com.internship.security;

import com.internship.model.User;
import com.internship.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/** Created by SecurityConfig (not a bean) so Boot does not also register it as a servlet filter. */
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwt;
    private final UserRepository users;

    public JwtAuthFilter(JwtUtil jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwt.validateToken(token)) {
                String email = jwt.extractClaims(token).getSubject();
                // Re-checking status on every request means locking an account takes effect immediately.
                users.findByEmail(email).filter(u -> u.getStatus() == User.Status.ACTIVE).ifPresent(u -> {
                    var auth = new UsernamePasswordAuthenticationToken(u, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }
        chain.doFilter(req, res);
    }
}
