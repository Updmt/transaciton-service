package com.testpayments.transacitonservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransacitonServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransacitonServiceApplication.class, args);
	}

}
