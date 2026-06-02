package com.dtf.reading_tracker_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReadingTrackerServerApplication {

	public static void main(String[] args) {
		System.out.println("DB URL: " + System.getenv("DATABASE_URL"));
		System.out.println("DB USER: " + System.getenv("DATABASE_USER"));
		System.out.println("DB PASS: " + System.getenv("DATABASE_PASSWORD"));
		SpringApplication.run(ReadingTrackerServerApplication.class, args);
	}

}
