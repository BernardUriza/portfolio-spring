package com.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PortfolioSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioSpringApplication.class, args);
	}

}
