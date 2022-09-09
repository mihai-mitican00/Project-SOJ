package com.endava.tmd.bookclubproject.registration;

import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.exception.ApiNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@WebMvcTest(RegistrationController.class)
@WithMockUser
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("register(..) throws bad request when email is invalid")
    void registerShouldThrowBadRequest() throws Exception {
        RegistrationRequest registrationRequest = new RegistrationRequest(
                "first",
                "last",
                "test123",
                "email.com",
                "asd"
        );

        when(registrationService.register(registrationRequest)).thenThrow(ApiBadRequestException.class);

        String jsonRequest = objectMapper.writeValueAsString(registrationRequest);
        mockMvc.perform(post("/register")
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest).with(csrf()))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(APPLICATION_JSON)
                )
                .andDo(print());

        verify(registrationService).register(registrationRequest);
    }

    @Test
    @DisplayName("register(..) returns token that was sent by mail")
    void registerShouldReturnToken() throws Exception {
        RegistrationRequest registrationRequest = new RegistrationRequest(
                "first",
                "last",
                "test123",
                "email@endava.com",
                "asd"
        );

        String token = UUID.randomUUID().toString();
        when(registrationService.register(registrationRequest)).thenReturn(token);

        String jsonRequest = objectMapper.writeValueAsString(registrationRequest);
        mockMvc.perform(post("/register")
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest).with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        content().string(token)
                )
                .andDo(print());

        verify(registrationService).register(registrationRequest);


    }

    @Test
    @DisplayName("confirmToken(..) throws not found when token does not exist")
    void confirmTokenShouldThrowNotFound() throws Exception {
        String token = UUID.randomUUID().toString();
        when(registrationService.confirmToken(token)).thenThrow(ApiNotFoundException.class);

        mockMvc.perform(get("/register/confirm").param("token", token))
                .andExpect(status().isNotFound())
                .andDo(print());

        verify(registrationService).confirmToken(token);
    }

    @Test
    @DisplayName("confirmToken(..) throws bad request when token is not valid")
    void confirmTokenShouldThrowBadRequest() throws Exception {
        String token = UUID.randomUUID().toString();
        when(registrationService.confirmToken(token)).thenThrow(ApiBadRequestException.class);

        mockMvc.perform(get("/register/confirm").param("token", token))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(registrationService).confirmToken(token);
    }

    @Test
    @DisplayName("confirmToken(..) returns confirmation message when token is valid")
    void confirmTokenShouldReturnConfirmMessage() throws Exception {
        String token = UUID.randomUUID().toString();
        when(registrationService.confirmToken(token)).thenReturn("confirmation message");

        mockMvc.perform(get("/register/confirm").param("token", token))
                .andExpectAll(
                        status().isOk(),
                        content().string("confirmation message")
                )
                .andDo(print());

        verify(registrationService).confirmToken(token);
    }

}