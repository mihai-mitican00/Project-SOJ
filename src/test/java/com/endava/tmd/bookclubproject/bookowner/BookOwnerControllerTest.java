package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static com.endava.tmd.bookclubproject.security.UserRoles.USER;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@WebMvcTest(BookOwnerController.class)
@WithMockUser
class BookOwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookOwnerService bookOwnerService;

    @Autowired
    private ObjectMapper objectMapper;

    private static List<BookOwner> entries;

    @BeforeAll
    static void setUp() {
        User user1 = new User("user1", "user1", "user1", "user1", "user1", USER);
        User user2 = new User("user2", "user2", "user2", "user2", "user2", USER);
        user1.setId(1L);
        user2.setId(2L);

        Book book1 = new Book("book1", "book1", "book1");
        Book book2 = new Book("book2", "book2", "book2");
        book1.setId(1L);
        book2.setId(2L);

        BookOwner entry1 = new BookOwner(book1, user1);
        BookOwner entry2 = new BookOwner(book2, user2);
        entries = List.of(entry1, entry2);
    }

    @Test
    @DisplayName("getAllBookOwners() returns no content when no entry found")
    void getAllShouldReturnNoContent() throws Exception {
        when(bookOwnerService.getAllBookOwners()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/book_owners"))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(bookOwnerService).getAllBookOwners();
    }

    @Test
    @DisplayName("getAllBookOwners() returns entries when entry found")
    void getAllShouldReturnOk() throws Exception {
        when(bookOwnerService.getAllBookOwners()).thenReturn(entries);

        String expectedEntries = objectMapper.writeValueAsString(entries);
        mockMvc.perform(get("/book_owners"))
                .andExpectAll(
                        status().isOk(),
                        content().json(expectedEntries, true)
                )
                .andDo(print());

        verify(bookOwnerService).getAllBookOwners();
    }

    @Test
    @DisplayName("addBookAsUser(..) throws bad request when book data is invalid")
    void addBookAsUserShouldThrowBadRequest() throws Exception {
        User validUser = entries.get(0).getUser();

        doThrow(ApiBadRequestException.class).when(bookOwnerService).addBookAsUser(validUser, null);

        mockMvc.perform(post("/book_owners")
                        .content("")
                        .with(user(validUser))
                        .with(csrf())
                )
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(bookOwnerService).addBookAsUser(validUser, null);
    }

    @Test
    @DisplayName("addBookAsUser(..) add book when book data is valid.")
    void addBookAsUserShouldAddBook() throws Exception {
        User validUser = entries.get(0).getUser();
        Book validBook = entries.get(0).getBook();

        String requestBody = objectMapper.writeValueAsString(validBook);
        String createdMessage = String.format("Thank you %s for adding \"%s\" by %s",
                validUser.getUsername(),
                validBook.getTitle(),
                validBook.getAuthor());

        mockMvc.perform(post("/book_owners")
                        .with(user(validUser))
                        .with(csrf())
                        .content(requestBody)
                        .contentType(APPLICATION_JSON)
                )
                .andExpectAll(
                        status().isCreated(),
                        content().string(createdMessage)
                )
                .andDo(print());
    }

    @Test
    @DisplayName("deleteBookAsUser(..) throws bad request when book not found or ownership not true")
    void deleteBookShouldThrowBadRequest() throws Exception {
        User validUser = entries.get(0).getUser();
        Long ownerId = validUser.getId();
        Long bookId = 3L;

        doThrow(ApiBadRequestException.class).when(bookOwnerService).deleteBookAsUser(bookId, ownerId);

        mockMvc.perform(delete("/book_owners")
                        .with(user(validUser))
                        .with(csrf())
                        .param("bookId", String.valueOf(bookId)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(bookOwnerService).deleteBookAsUser(bookId, ownerId);
    }

    @Test
    @DisplayName("deleteBookAsUser(..) deletes book when book found and ownership true")
    void deleteBookShouldDeleteBook() throws Exception {
        User validUser = entries.get(0).getUser();
        Long ownerId = validUser.getId();
        Long bookId = 2L;

        String message = String.format("You deleted book with id %d", bookId);
        mockMvc.perform(delete("/book_owners")
                        .with(user(validUser))
                        .with(csrf())
                        .param("bookId", String.valueOf(bookId)))
                .andExpectAll(
                        status().isOk(),
                        content().string(message)
                        )
                .andDo(print());
    }
}