package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.exception.ApiNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.endava.tmd.bookclubproject.security.UserRoles.ADMIN;
import static com.endava.tmd.bookclubproject.security.UserRoles.USER;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@WebMvcTest(value = UserController.class)
@WithMockUser
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private static User adminUser, normalUser;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        adminUser =
                new User("admin", "admin", "admin", "admin", "admin@company.com", ADMIN);
        normalUser =
                new User("normal", "normal", "normal", "normal", "normal@company.com", USER);
        adminUser.setId(1L);
        normalUser.setId(2L);
    }

    @Test
    @DisplayName("getAllUsers() returns no content when no users found")
    void getAllShouldReturnNoContent() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/users"))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("getAllUsers() returns users with status code ok when users found")
    void getAllShouldReturnOk() throws Exception {
        List<User> userList = List.of(adminUser, normalUser);
        when(userService.getAllUsers()).thenReturn(userList);

        String expectedUsers = objectMapper.writeValueAsString(userList);
        mockMvc.perform(get("/users"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$", hasSize(2)),
                        content().json(expectedUsers, true)
                )
                .andDo(print());

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("getUserById(..) throws not found when user not found")
    void getUserShouldThrowNotFound() throws Exception {
        Long id = 0L;
        when(userService.getUserById(id)).thenReturn(Optional.empty());

        String errorMessage = String.format("There is no user with id %d.", id);
        mockMvc.perform(get("/users/{id}", id))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(APPLICATION_JSON),
                        jsonPath("$.message", is(errorMessage))
                )
                .andDo(print());

        verify(userService).getUserById(id);
    }

    @Test
    @DisplayName("getUserById(..) throws forbidden when access restricted")
    void getUserShouldThrowForbidden() throws Exception {
        Long id = 1L;
        when(userService.getUserById(id)).thenReturn(Optional.of(adminUser));

        String errorMessage = "Normal user cannot see other users!";
        mockMvc.perform(get("/users/{id}", id).with(user(normalUser)))
                .andExpectAll(
                        status().isForbidden(),
                        content().contentType(APPLICATION_JSON),
                        jsonPath("$.message", is(errorMessage))
                )
                .andDo(print());

        verify(userService).getUserById(id);
    }

    @Test
    @DisplayName("getUserById(..) returns user when user found")
    void getUserShouldReturnOk() throws Exception {
        Long id = 2L;
        when(userService.getUserById(id)).thenReturn(Optional.of(normalUser));

        String expectedUser = objectMapper.writeValueAsString(normalUser);
        mockMvc.perform(get("/users/{id}", id).with(user(adminUser)))
                .andExpectAll(
                        status().isOk(),
                        content().json(expectedUser, true)
                )
                .andDo(print());

        verify(userService).getUserById(id);
    }

    @Test
    @DisplayName("getBooksOfUser(..) throws not found when no user found")
    void getBooksOfUserShouldThrowNotFound() throws Exception {
        Long id = 0L;
        when(userService.getUserById(id)).thenReturn(Optional.empty());

        String errorMessage = String.format("There is no user with id %d.", id);
        mockMvc.perform(get("/users/BooksOwned/{id}", id))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(APPLICATION_JSON),
                        jsonPath("$.message", is(errorMessage))
                )
                .andDo(print());

        verify(userService).getUserById(id);
    }

    @Test
    @DisplayName("getBooksOfUser(..) throws forbidden when access restricted")
    void getBooksOfUserShouldThrowForbidden() throws Exception {
        Long id = 1L;
        when(userService.getUserById(id)).thenReturn(Optional.ofNullable(adminUser));

        String errorMessage = "Normal user cannot see other user's books!";
        mockMvc.perform(get("/users/BooksOwned/{id}", id).with(user(normalUser)))
                .andExpectAll(
                        status().isForbidden(),
                        content().contentType(APPLICATION_JSON),
                        jsonPath("$.message", is(errorMessage))
                )
                .andDo(print());

        verify(userService).getUserById(id);
    }

    @Test
    @DisplayName("getBooksOfUser(..) returns no content when user has no books")
    void getBooksOfUserShouldReturnNoContent() throws Exception {
        Long id = normalUser.getId();
        when(userService.getUserById(id)).thenReturn(Optional.of(normalUser));

        mockMvc.perform(get("/users/BooksOwned/{id}", id).with(user(normalUser)))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(userService).getUserById(id);
    }

    @Test
    @DisplayName("getBooksOfUser(..) returns books when user found")
    void getBooksOfUserShouldReturnOk() throws Exception {
        Long id = 2L;
        when(userService.getUserById(id)).thenReturn(Optional.of(normalUser));

        List<Book> booksOwned = List.of(new Book("title", "author", "edition"));
        when(userService.getBooksOfUser(id)).thenReturn(booksOwned);

        String expectedJson = objectMapper.writeValueAsString(booksOwned);
        mockMvc.perform(get("/users/BooksOwned/{id}", id).with(user(normalUser)))
                .andExpectAll(
                        status().isOk(),
                        content().json(expectedJson, true)
                )
                .andDo(print());

        verify(userService).getUserById(id);
        verify(userService).getBooksOfUser(id);
    }

    @Test
    @DisplayName("deleteUser(..) throws not found when no user found")
    void deleteUserShouldThrowNotFound() throws Exception {
        Long id = 0L;
        String errorMessage = "errorMessage";

        doThrow(new ApiNotFoundException(errorMessage)).when(userService).deleteUser(id);
        mockMvc.perform(delete("/users/{id}", id).with(csrf()))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(APPLICATION_JSON),
                        jsonPath("$.message", is(errorMessage))
                )
                .andDo(print());

        verify(userService).deleteUser(id);
    }

    @Test
    @DisplayName("deleteUser(..) delete user when user found")
    void deleteUserShouldReturnOk() throws Exception {
        Long id = normalUser.getId();
        String email = normalUser.getEmail();
        String message = String.format("User with email %s and all his traces deleted from system!", email);

        when(userService.deleteUser(id)).thenReturn(message);
        mockMvc.perform(delete("/users/{id}", id).with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        content().string(message)
                )
                .andDo(print());

        verify(userService).deleteUser(id);
    }
}
