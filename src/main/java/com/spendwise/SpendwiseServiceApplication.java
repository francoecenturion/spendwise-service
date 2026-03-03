package com.spendwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SpendwiseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpendwiseServiceApplication.class, args);
	}

}
