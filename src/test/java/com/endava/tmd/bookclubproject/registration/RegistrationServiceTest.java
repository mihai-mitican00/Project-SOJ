package com.endava.tmd.bookclubproject.registration;

import com.endava.tmd.bookclubproject.email.EmailSender;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.exception.ApiNotFoundException;
import com.endava.tmd.bookclubproject.registration.token.ConfirmationToken;
import com.endava.tmd.bookclubproject.registration.token.ConfirmationTokenService;
import com.endava.tmd.bookclubproject.security.UserRoles;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserService;
import org.checkerframework.checker.units.qual.C;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.endava.tmd.bookclubproject.security.UserRoles.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ConfirmationTokenService confirmationTokenService;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private RegistrationService registrationService;

    private RegistrationRequest request;
    private User user;
    private ConfirmationToken confirmationToken;

    @BeforeEach
    void setUp() {
        request = new RegistrationRequest(
                "first",
                "last",
                "test123",
                "email@endava.com",
                "asd"
        );

        user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                USER
        );

        String token = UUID.randomUUID().toString();
        confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(14),
                user
        );
    }


    @Test
    @DisplayName("register(..) throws bad request when email is invalid")
    void registerShouldThrowBadRequestInvalidMail() {
        request.setEmail("test@mail.com");
        String email = request.getEmail();
        String errorMessage = String.format("email %s is not a valid email", email);
        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("register(..) throws bad request when token creation fails")
    void registerShouldThrowBadRequestCannotCreateToken() {

        when(userService.registerUser(Optional.of(user))).thenThrow(ApiBadRequestException.class);

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(ApiBadRequestException.class);

        verify(userService).registerUser(Optional.of(user));
    }

    @Test
    @DisplayName("register(..) returns token sent by email")
    void registerShouldReturnTokenSentByMail() {
        String email = request.getEmail();
        String token = UUID.randomUUID().toString();
        when(userService.registerUser(Optional.of(user))).thenReturn(token);

        assertThat(registrationService.register(request))
                .isNotEmpty()
                .isEqualTo(token);

        String activationLink = "http://localhost:8080/register/confirm?token=" + token;
        String emailSent = registrationService.buildEmail(request.getFirstName(), activationLink);
        verify(userService).registerUser(Optional.of(user));
        verify(emailSender).sendEmail(email, emailSent);
    }

    @Test
    @DisplayName("confirmToken(..) throws not found when token not found")
    void confirmTokenShouldThrowNotFound(){
        String token = UUID.randomUUID().toString();
        when(confirmationTokenService.getToken(token)).thenReturn(Optional.empty());

        String errorMessage = "Token not found!";
        assertThatThrownBy(() -> registrationService.confirmToken(token))
                .isInstanceOf(ApiNotFoundException.class)
                .hasMessage(errorMessage);

        verify(confirmationTokenService).getToken(token);
    }

    @Test
    @DisplayName("confirmToken(..) throws bad request when token was already confirmed")
    void confirmTokenShouldThrowBadRequestTokenAlreadyConfirmed(){
        String token = confirmationToken.getToken();
        confirmationToken.setConfirmedAt(LocalDateTime.now());

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.of(confirmationToken));

        String errorMessage = "Token was already confirmed!";
        assertThatThrownBy(() -> registrationService.confirmToken(token))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(confirmationTokenService).getToken(token);
    }

    @Test
    @DisplayName("confirmToken(..) throws bad request when token was already confirmed")
    void confirmTokenShouldThrowBadRequestTokenExpired(){
        String token = confirmationToken.getToken();
        confirmationToken.setExpiredAt(LocalDateTime.now().minusDays(1));

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.of(confirmationToken));

        String errorMessage = "Token is expired!";
        assertThatThrownBy(() -> registrationService.confirmToken(token))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(confirmationTokenService).getToken(token);
    }
    @Test
    @DisplayName("confirmToken(..) returns confirmation message after enabling account")
    void confirmTokenShouldReturnConfirmationMessage(){
        String token = confirmationToken.getToken();

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.of(confirmationToken));

        String expectedMessage = String.format("Account with email %s was confirmed!", confirmationToken.getUser().getEmail());
        String actualMessage = registrationService.confirmToken(token);

        assertThat(actualMessage).isEqualTo(expectedMessage);

        verify(confirmationTokenService).getToken(token);
        verify(confirmationTokenService).setConfirmedAt(token);
        verify(userService).enableUserAccount(confirmationToken.getUser().getEmail());
    }

}