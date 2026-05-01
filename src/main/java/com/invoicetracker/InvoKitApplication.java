package com.invoicetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InvoKitApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvoKitApplication.class, args);
	}

}