package com.project.movierec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MovieRecommendationApplication {

	public static void main(String[] eloquenceArgs) {
		SpringApplication.run(MovieRecommendationApplication.class, eloquenceArgs);
	}
}
