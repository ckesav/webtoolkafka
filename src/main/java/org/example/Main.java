package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
/**
 * Main entry point.
 */
@SpringBootApplication()
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(final String[] args)
    {
        logger.info("Starting Spring Boot");
        SpringApplication.run(Main.class, args);
    }
}