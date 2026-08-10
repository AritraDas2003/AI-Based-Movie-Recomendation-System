package com.project.movierec.core;

import tools.jackson.databind.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;
import java.util.*;

@Service
class TmdbService {

    private final RestTemplate http = new RestTemplate();
    private final String base, token;

    TmdbService(@Value("${tmdb.api.url}") String b, @Value("${tmdb.api.token}") String t) {
        base = b;
        token = t;
    }

    private JsonNode get(String path) {
        if (token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "TMDB_API_TOKEN is not configured");
        }
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return http.exchange(base + path, HttpMethod.GET, new HttpEntity<>(h), JsonNode.class).getBody();
    }

    @Cacheable("trendingMovies")
    JsonNode trending() {
        return get("/trending/movie/week");
    }

    @Cacheable("popularMovies")
    JsonNode popular() {
        return get("/movie/popular");
    }

    @Cacheable("genresList")
    JsonNode genres() {
        return get("/genre/movie/list");
    }

    @Cacheable(value = "movieDetails", key = "#id")
    JsonNode details(Long id) {
        return get("/movie/" + id + "?append_to_response=credits");
    }

    @Cacheable(value = "searchCache", key = "#q")
    JsonNode search(String q) {
        return get("/search/movie?query=" + UriUtils.encodeQueryParam(q, java.nio.charset.StandardCharsets.UTF_8));
    }

    JsonNode similar(Long id) {
        return get("/movie/" + id + "/similar");
    }

    JsonNode discover(String genres, Integer min, Integer max) {
        String p = "/discover/movie?sort_by=vote_average.desc&vote_count.gte=100";
        if (genres != null && !genres.isBlank()) {
            p += "&with_genres=" + genres;
        }
        if (min != null) {
            p += "&primary_release_date.gte=" + min + "-01-01";
        }
        if (max != null) {
            p += "&primary_release_date.lte=" + max + "-12-31";
        }
        return get(p);
    }

    ResponseEntity<byte[]> image(String size, String file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return http.exchange(
                "https://image.tmdb.org/t/p/" + size + "/" + file,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class
        );
    }
}