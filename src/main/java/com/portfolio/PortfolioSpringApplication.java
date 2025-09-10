package com.portfolio;

import com.portfolio.config.FeatureFlagsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(FeatureFlagsConfig.class)
public class PortfolioSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioSpringApplication.class, args);
	}

}
