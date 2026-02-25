package com.vivek.clubRegistration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClubRegistrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClubRegistrationApplication.class, args);
        System.out.println("\n==========================================");
        System.out.println("  ⭐ Exclusive Club Portal is RUNNING!   ");
        System.out.println("  Registration : http://localhost:8080    ");
        System.out.println("  Admin Panel  : http://localhost:8080/admin");
        System.out.println("==========================================\n");
	}

}
