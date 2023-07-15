package com.bandall.location_share;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LocationShareApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocationShareApplication.class, args);
	}

}
