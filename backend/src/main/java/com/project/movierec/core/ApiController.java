package com.project.movierec.core;

import tools.jackson.databind.*;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.security.crypto.password.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
class ApiController {

    private final UserRepository users;
    private final WatchlistRepository watch;
    private final HistoryRepository history;
    private final RatingRepository ratings;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final TmdbService tmdb;
    private final RestTemplate http = new RestTemplate();
    private final String aiUrl;

    ApiController(UserRepository u, WatchlistRepository w, HistoryRepository h, RatingRepository r,
                  PasswordEncoder e, JwtService j, TmdbService t,
                  @org.springframework.beans.factory.annotation.Value("${ai.service.url}") String a) {
        users = u;
        watch = w;
        history = h;
        ratings = r;
        encoder = e;
        jwt = j;
        tmdb = t;
        aiUrl = a;
    }

    record Credentials(@NotBlank String email, @NotBlank String password, String username) {}

    record RatingInput(@NotNull Long tmdbMovieId, @DecimalMin("1.0") @DecimalMax("5.0") Double rating,
                        String reviewText) {}

    // ---------------------------------------------------------------
    // Auth
    // ---------------------------------------------------------------

    @PostMapping("/auth/register")
    ResponseEntity<?> register(@RequestBody Credentials c) {
        if (c.username() == null || c.username().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is required"));
        }
        if (users.existsByEmail(c.email().toLowerCase()) || users.existsByUsername(c.username())) {
            return ResponseEntity.status(409).body(Map.of("message", "Email or username already exists"));
        }
        User u = new User();
        u.setEmail(c.email());
        u.setUsername(c.username());
        u.setPasswordHash(encoder.encode(c.password()));
        users.save(u);
        return ResponseEntity.status(201).body(auth(u));
    }

    @PostMapping("/auth/login")
    ResponseEntity<?> login(@RequestBody Credentials c) {
        return users.findByEmail(c.email().toLowerCase())
                .filter(u -> encoder.matches(c.password(), u.getPasswordHash()))
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(auth(u)))
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Invalid email or password")));
    }

    @GetMapping("/auth/me")
    Map<String, Object> me(Authentication a) {
        return userDto(current(a));
    }

    private Map<String, Object> auth(User u) {
        return Map.of("token", jwt.token(u), "user", userDto(u));
    }

    private Map<String, Object> userDto(User u) {
        return Map.of("id", u.getId(), "username", u.getUsername(), "email", u.getEmail());
    }

    private User current(Authentication a) {
        return (User) a.getPrincipal();
    }

    // ---------------------------------------------------------------
    // Health
    // ---------------------------------------------------------------

    @GetMapping("/health")
    Map<String, String> health() {
        return Map.of("status", "healthy", "service", "CineMatch backend");
    }

    // ---------------------------------------------------------------
    // Movies
    // ---------------------------------------------------------------

    @GetMapping("/movies/trending")
    JsonNode trending() {
        return tmdb.trending();
    }

    @GetMapping("/movies/popular")
    JsonNode popular() {
        return tmdb.popular();
    }

    @GetMapping("/movies/genres")
    JsonNode genres() {
        return tmdb.genres();
    }

    @GetMapping("/movies/search")
    JsonNode search(@RequestParam String query) {
        return tmdb.search(query);
    }

    @GetMapping("/movies/{id}")
    JsonNode movie(@PathVariable Long id) {
        return tmdb.details(id);
    }

    @GetMapping("/images/{size}/{file:.+}")
    ResponseEntity<byte[]> image(@PathVariable String size, @PathVariable String file) {
        return tmdb.image(size, file);
    }

    // ---------------------------------------------------------------
    // Watchlist
    // ---------------------------------------------------------------

    @GetMapping("/watchlist")
    List<JsonNode> listWatch(Authentication a) {
        return watch.findByUser(current(a)).stream()
                .map(x -> tmdb.details(x.tmdbMovieId))
                .toList();
    }

    @PostMapping("/watchlist/{id}")
    ResponseEntity<?> addWatch(@PathVariable Long id, Authentication a) {
        User u = current(a);
        if (watch.findByUserAndTmdbMovieId(u, id).isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Already in watchlist"));
        }
        WatchlistItem x = new WatchlistItem();
        x.user = u;
        x.tmdbMovieId = id;
        watch.save(x);
        return ResponseEntity.status(201).body(Map.of("message", "Added to watchlist"));
    }

    @DeleteMapping("/watchlist/{id}")
    void removeWatch(@PathVariable Long id, Authentication a) {
        watch.findByUserAndTmdbMovieId(current(a), id).ifPresent(watch::delete);
    }

    // ---------------------------------------------------------------
    // Watched history
    // ---------------------------------------------------------------

    @GetMapping("/history")
    List<JsonNode> listHistory(Authentication a) {
        return history.findByUser(current(a)).stream()
                .map(x -> tmdb.details(x.tmdbMovieId))
                .toList();
    }

    @PostMapping("/history/{id}")
    ResponseEntity<?> addHistory(@PathVariable Long id, Authentication a) {
        User u = current(a);
        if (history.findByUserAndTmdbMovieId(u, id).isEmpty()) {
            WatchedHistory x = new WatchedHistory();
            x.user = u;
            x.tmdbMovieId = id;
            history.save(x);
        }
        return ResponseEntity.status(201).body(Map.of("message", "Marked as watched"));
    }

    @DeleteMapping("/history/{id}")
    void removeHistory(@PathVariable Long id, Authentication a) {
        history.findByUserAndTmdbMovieId(current(a), id).ifPresent(history::delete);
    }

    // ---------------------------------------------------------------
    // Ratings
    // ---------------------------------------------------------------

    @PostMapping("/ratings")
    ResponseEntity<?> rate(@RequestBody RatingInput i, Authentication a) {
        User u = current(a);
        MovieRating r = ratings.findByUserAndTmdbMovieId(u, i.tmdbMovieId()).orElseGet(MovieRating::new);
        r.user = u;
        r.tmdbMovieId = i.tmdbMovieId();
        r.rating = i.rating();
        r.reviewText = i.reviewText();
        r.updatedAt = Instant.now();
        ratings.save(r);
        return ResponseEntity.ok(Map.of("message", "Rating saved"));
    }

    @GetMapping("/ratings/user")
    List<Map<String, Object>> myRatings(Authentication a) {
        return ratings.findByUser(current(a)).stream()
                .map(this::ratingDto)
                .toList();
    }

    @GetMapping("/ratings/movie/{id}")
    List<Map<String, Object>> movieRatings(@PathVariable Long id) {
        return ratings.findByTmdbMovieId(id).stream()
                .map(this::ratingDto)
                .toList();
    }

    private Map<String, Object> ratingDto(MovieRating r) {
        return Map.of(
                "movieId", r.tmdbMovieId,
                "rating", r.rating,
                "reviewText", r.reviewText == null ? "" : r.reviewText,
                "username", r.user.getUsername()
        );
    }

    // ---------------------------------------------------------------
    // AI recommendations
    // ---------------------------------------------------------------

    @PostMapping("/recommendations/ai")
    ResponseEntity<?> recommend(@RequestBody Map<String, String> body, Authentication a) {
        String prompt = body.getOrDefault("prompt", "").trim();
        if (prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Prompt is required"));
        }

        JsonNode p;
        try {
            p = http.postForObject(aiUrl + "/api/recommendations/parse-prompt", Map.of("prompt", prompt), JsonNode.class);
        } catch (RestClientException e) {
            return ResponseEntity.status(503).body(Map.of("message", "AI service is unavailable"));
        }

        Set<Long> excluded = new HashSet<>();
        User u = current(a);
        history.findByUser(u).forEach(x -> excluded.add(x.tmdbMovieId));
        ratings.findByUser(u).stream()
                .filter(x -> x.rating < 3)
                .forEach(x -> excluded.add(x.tmdbMovieId));

        Map<Long, JsonNode> candidates = new LinkedHashMap<>();
        if (p.path("reference_movie").isTextual()) {
            JsonNode found = tmdb.search(p.path("reference_movie").asText());
            if (found.path("results").isArray() && found.path("results").size() > 0) {
                tmdb.similar(found.path("results").get(0).path("id").asLong())
                        .path("results")
                        .forEach(x -> candidates.put(x.path("id").asLong(), x));
            }
        }

        String genreIds = p.path("genre_ids").isArray()
                ? StreamSupport.stream(p.path("genre_ids").spliterator(), false)
                        .map(JsonNode::asText)
                        .collect(Collectors.joining(","))
                : "";

        tmdb.discover(
                genreIds,
                p.path("year_min").isInt() ? p.path("year_min").asInt() : null,
                p.path("year_max").isInt() ? p.path("year_max").asInt() : null
        ).path("results").forEach(x -> candidates.putIfAbsent(x.path("id").asLong(), x));

        List<Map<String, Object>> result = candidates.values().stream()
                .filter(x -> !excluded.contains(x.path("id").asLong()))
                .sorted(Comparator.comparingDouble((JsonNode x) -> x.path("vote_average").asDouble()).reversed())
                .limit(20)
                .map(x -> Map.<String, Object>of(
                        "movie", x,
                        "score", Math.round(x.path("vote_average").asDouble() * 10),
                        "reasons", List.of("Matches your AI preference", "Popular and highly rated")
                ))
                .toList();

        return ResponseEntity.ok(Map.of("parsedPrompt", p, "recommendations", result));
    }
}