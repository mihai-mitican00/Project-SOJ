package com.endava.tmd.bookclubproject;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class BookClubProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookClubProjectApplication.class, args);
	}

	@Bean
	public OpenAPI openApiConfig(){
		return new OpenAPI().info(apiInfo());
	}

	public Info apiInfo(){
		Info info = new Info();
		info.title("TMD-BookClub API").description("Swagger API for Book Club Project").version("1.0.0");
		return info;
	}
}
