package com.project.movierec.core;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "watchlist_items", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tmdb_movie_id"}))
class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    User user;

    @Column(nullable = false)
    Long tmdbMovieId;

    Instant addedAt = Instant.now();
}

@Entity
@Table(name = "watched_history", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tmdb_movie_id"}))
class WatchedHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    User user;

    @Column(nullable = false)
    Long tmdbMovieId;

    Instant watchedAt = Instant.now();
}

@Entity
@Table(name = "movie_ratings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tmdb_movie_id"}))
class MovieRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    User user;

    @Column(nullable = false)
    Long tmdbMovieId;

    @Column(nullable = false)
    Double rating;

    @Column(length = 2000)
    String reviewText;

    Instant updatedAt = Instant.now();
}