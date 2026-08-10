package com.project.movierec.core;

import org.springframework.security.core.context.SecurityContextHolder;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.*;
import org.springframework.stereotype.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.*;
import java.nio.charset.*;
import java.util.*;

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain chain(HttpSecurity h, JwtFilter f) throws Exception {
        return h.csrf(c -> c.disable())
                .cors(c -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/**", "/api/movies/**", "/api/images/**", "/api/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(f, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

@Service
class JwtService {

    private final byte[] key;
    private final long expiry;

    JwtService(@Value("${security.jwt.secret-key}") String s, @Value("${security.jwt.expiration-time}") long e) {
        key = Arrays.copyOf(s.getBytes(StandardCharsets.UTF_8), 32);
        expiry = e;
    }

    String token(User u) {
        return Jwts.builder()
                .setSubject(u.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(Keys.hmacShaKeyFor(key), SignatureAlgorithm.HS256)
                .compact();
    }

    String email(String t) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(t)
                .getBody()
                .getSubject();
    }
}

@Component
class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserRepository users;

    JwtFilter(JwtService j, UserRepository u) {
        jwt = j;
        users = u;
    }

    protected void doFilterInternal(HttpServletRequest r, HttpServletResponse s, FilterChain c)
            throws ServletException, IOException {
        String h = r.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            try {
                User u = users.findByEmail(jwt.email(h.substring(7))).orElse(null);
                if (u != null) {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    u, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
                }
            } catch (JwtException ignored) {
            }
        }
        c.doFilter(r, s);
    }
}