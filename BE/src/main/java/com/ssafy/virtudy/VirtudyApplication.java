package com.ssafy.virtudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.scheduling.annotation.EnableScheduling;

<<<<<<< HEAD
@EnableScheduling
@EnableJpaAuditing
=======
>>>>>>> dad092f (add: 티어 및 리포트 관련 API)
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class VirtudyApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtudyApplication.class, args);
	}

}
