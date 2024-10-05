package com.ojasare.logindemo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LogindemoApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(e -> {
			if (System.getProperty(e.getKey()) == null) {
				System.setProperty(e.getKey(), e.getValue());
			}
		});
		SpringApplication.run(LogindemoApplication.class, args);
	}

}
