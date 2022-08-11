package com.endava.tmd.bookclubproject;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.email.EmailService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import java.util.Optional;


@SpringBootApplication
public class BookClubProjectApplication {

	@Autowired
	private EmailService emailService;
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

//	@EventListener(ApplicationReadyEvent.class)
//	public void sendMail(){
//		emailService.sendEmail("mihai.mitican00@e-uvt.ro", "asd", "this is email");
//	}
}
