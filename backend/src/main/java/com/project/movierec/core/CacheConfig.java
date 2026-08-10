package com.project.movierec.core;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CacheConfig {

    @Bean
    CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "trendingMovies",
                "popularMovies",
                "movieDetails",
                "genresList",
                "searchCache"
        );
    }
}