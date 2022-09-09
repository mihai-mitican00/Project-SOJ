package com.endava.tmd.bookclubproject.security;

import com.endava.tmd.bookclubproject.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import static com.endava.tmd.bookclubproject.security.UserRoles.ADMIN;
import static com.endava.tmd.bookclubproject.security.UserRoles.USER;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration{

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;


        @Bean
        protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
   //             .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
          //      .and()
                .authorizeRequests().antMatchers("/", "index", "/register/**").permitAll()
                .anyRequest().authenticated()
                .and()
                //.formLogin();
                .httpBasic();
          //      .and()
//                .formLogin().permitAll()
//                .loginPage("/login")
//                .usernameParameter("username")
//                .passwordParameter("password")
//               // .successHandler(authenticationSuccessHandler())
//                //.defaultSuccessUrl("/mainPage", true)
//                .and()
//                .logout()
//                .logoutUrl("/logout")
//                .clearAuthentication(true)
//                .invalidateHttpSession(true)
//                .deleteCookies("JSESSIONID")
//                .logoutSuccessUrl("/");


        return http.build();
    }

//    @Bean
//    public ProviderManager providerManager(){
//            return new ProviderManager(daoAuthenticationProvider());
//    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
            DaoAuthenticationProvider daoAuthenticationProvider =
                    new DaoAuthenticationProvider();
            daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
            daoAuthenticationProvider.setUserDetailsService(userService);
            return daoAuthenticationProvider;
    }


    //    @Bean
//    public InMemoryUserDetailsManager userDetailsService() {
//        UserDetails adminUser = User.builder()
//                .username("admin")
//                .password(passwordEncoder.encode("asd"))
//                .authorities(ADMIN.getGrantedAuthorities())
//                .build();
//
//        UserDetails casualUser = User.builder()
//                .username("casual")
//                .password(passwordEncoder.encode("asd"))
//                .authorities(USER.getGrantedAuthorities())
//                .build();
//        return new InMemoryUserDetailsManager(adminUser, casualUser);
//    }
}

