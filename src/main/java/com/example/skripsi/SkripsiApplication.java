package com.example.skripsi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SkripsiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkripsiApplication.class, args);
	}

}
