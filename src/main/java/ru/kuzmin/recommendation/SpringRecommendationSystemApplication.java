package ru.kuzmin.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringRecommendationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRecommendationSystemApplication.class, args);
    }

}
