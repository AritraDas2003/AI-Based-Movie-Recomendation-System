package com.project.movierec.core;

import org.springframework.data.jpa.repository.*;
import java.util.*;

interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}

interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findByUser(User u);
    Optional<WatchlistItem> findByUserAndTmdbMovieId(User u, Long id);
}

interface HistoryRepository extends JpaRepository<WatchedHistory, Long> {
    List<WatchedHistory> findByUser(User u);
    Optional<WatchedHistory> findByUserAndTmdbMovieId(User u, Long id);
}

interface RatingRepository extends JpaRepository<MovieRating, Long> {
    List<MovieRating> findByUser(User u);
    Optional<MovieRating> findByUserAndTmdbMovieId(User u, Long id);
    List<MovieRating> findByTmdbMovieId(Long id);
}