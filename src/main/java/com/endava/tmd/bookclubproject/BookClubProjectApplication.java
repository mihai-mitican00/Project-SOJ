package com.endava.tmd.bookclubproject;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.email.EmailService;
import com.endava.tmd.bookclubproject.security.SecurityConfiguration;
import com.endava.tmd.bookclubproject.security.UserRoles;
import com.endava.tmd.bookclubproject.user.User;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.apache.catalina.core.ApplicationServletRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.event.EventListener;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;

import static com.endava.tmd.bookclubproject.security.UserRoles.USER;
import static org.springframework.context.annotation.FilterType.CUSTOM;


@SpringBootApplication

public class BookClubProjectApplication {

//	@Autowired
//	private EmailService emailService;

    public static void main(String[] args) {
        SpringApplication.run(BookClubProjectApplication.class, args);
    }

    @Bean
    public OpenAPI openApiConfig() {
        return new OpenAPI().info(apiInfo());
    }

    public Info apiInfo() {
        Info info = new Info();
        info.title("TMD-BookClub API").description("Swagger API for Book Club Project").version("1.0.0");
        return info;
    }

//	@EventListener(ApplicationReadyEvent.class)
//	public void sendMail(){
//		emailService.sendEmail("mihai.mitican00@e-uvt.ro", "asd", "this is email");
//	}
}
