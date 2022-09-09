package com.endava.tmd.bookclubproject.bookborrower;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.security.UserRoles;
import com.endava.tmd.bookclubproject.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static com.endava.tmd.bookclubproject.security.UserRoles.USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@WebMvcTest(BookBorrowerController.class)
@WithMockUser
class BookBorrowerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookBorrowerService bookBorrowerService;

    @Autowired
    private ObjectMapper objectMapper;

    private static List<BookBorrower> entries;

    @BeforeAll
    static void setUp() {
        Book book1 = new Book("book1", "book1", "book1");
        Book book2 = new Book("book2", "book2", "book2");
        book1.setId(1L);
        book2.setId(2L);

        User borrower = new User(
                        "borrower", "borrower", "borrower", "borrower", "borrower", USER
        );
        borrower.setId(1L);

        Long ownerId = 2L;
        Long rentingWeeks = 2L;

        BookBorrower entry1 = new BookBorrower(book1, borrower, ownerId, rentingWeeks);
        BookBorrower entry2 = new BookBorrower(book1, borrower, ownerId, rentingWeeks);

        entries = List.of(entry1, entry2);
    }

    @Test
    @DisplayName("getAllBookBorrowers() returns no content when no entry found")
    void getAllShouldReturnNoContent() throws Exception {
        when(bookBorrowerService.getAllBookBorrowers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/book_borrowers"))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(bookBorrowerService).getAllBookBorrowers();
    }

    @Test
    @DisplayName("getAllBookBorrowers() returns entries when entry found")
    void getAllShouldReturnOk() throws Exception {
        when(bookBorrowerService.getAllBookBorrowers()).thenReturn(entries);

        String expectedEntries = objectMapper.writeValueAsString(entries);
        mockMvc.perform(get("/book_borrowers"))
                .andExpectAll(
                        status().isOk(),
                        content().json(expectedEntries)
                )
                .andDo(print());

        verify(bookBorrowerService).getAllBookBorrowers();
    }

    @Test
    @DisplayName("getBooksThatUserGave(..) returns no content when no books found")
    void getBooksThatUserGaveShouldReturnNoContent() throws Exception {
        when(bookBorrowerService.getBorrowsWhereUserGave(0L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/book_borrowers/BooksUserGave").param("ownerId", String.valueOf(0L)))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(bookBorrowerService).getBorrowsWhereUserGave(0L);
    }

    @Test
    @DisplayName("getBooksThatUserGave(..) returns books when books found")
    void getBooksThatUserGaveShouldReturnOk() throws Exception {
        Long ownerId = 2L;
        when(bookBorrowerService.getBorrowsWhereUserGave(ownerId)).thenReturn(entries);
        when(bookBorrowerService.formatBooksThatUserGave(entries)).thenReturn("formattedEntries");

        mockMvc.perform(get("/book_borrowers/BooksUserGave").param("ownerId", String.valueOf(ownerId)))
                .andExpectAll(
                        status().isOk(),
                        content().string("formattedEntries")
                )
                .andDo(print());

        verify(bookBorrowerService).getBorrowsWhereUserGave(ownerId);
        verify(bookBorrowerService).formatBooksThatUserGave(entries);
    }

    @Test
    @DisplayName("getBooksThatUserRented(..) returns no content when no books found")
    void getBooksThatUserRentedShouldReturnNoContent() throws Exception {
        when(bookBorrowerService.getBorrowsWhereUserReceived(0L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/book_borrowers/BooksUserRented").param("borrowerId", String.valueOf(0L)))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(bookBorrowerService).getBorrowsWhereUserReceived(0L);
    }

    @Test
    @DisplayName("getBooksThatUserRented(..) returns books when books found")
    void getBooksThatUserRentedShouldReturnOk() throws Exception {
        Long borrowerId = 1L;
        when(bookBorrowerService.getBorrowsWhereUserReceived(borrowerId)).thenReturn(entries);
        when(bookBorrowerService.formatBooksThatUserReceived(entries)).thenReturn("formattedEntries");

        mockMvc.perform(get("/book_borrowers/BooksUserRented").param("borrowerId", String.valueOf(borrowerId)))
                .andExpectAll(
                        status().isOk(),
                        content().string("formattedEntries")
                )
                .andDo(print());

        verify(bookBorrowerService).getBorrowsWhereUserReceived(borrowerId);
        verify(bookBorrowerService).formatBooksThatUserReceived(entries);
    }

    @Test
    @DisplayName("borrowBookFromOwner(..) throws bad request when borrow cannot be done")
    void borrowBookShouldThrowBadRequest() throws Exception {
        User borrower = entries.get(0).getBorrower();
        Long bookId = 0L;
        Long borrowerId = borrower.getId();
        Long ownerId = 0L;
        Long weeksToRent = 0L;

        when(bookBorrowerService.borrowBookFromOwner(bookId, borrowerId, ownerId, weeksToRent))
                .thenThrow(ApiBadRequestException.class);

        mockMvc.perform(post("/book_borrowers")
                        .with(user(borrower)).with(csrf())
                        .param("bookId", bookId.toString())
                        .param("ownerId", ownerId.toString())
                        .param("weeks", weeksToRent.toString()))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(bookBorrowerService).borrowBookFromOwner(bookId, borrowerId, ownerId, weeksToRent);
    }

    @Test
    @DisplayName("borrowBookFromOwner(..) make borrow when borrow can be done")
    void borrowBookShouldReturnCreated() throws Exception {
        User borrower = entries.get(0).getBorrower();
        Long bookId = 2L;
        Long borrowerId = borrower.getId();
        Long ownerId = 2L;
        Long weeksToRent = 2L;

        when(bookBorrowerService.borrowBookFromOwner(bookId, borrowerId, ownerId, weeksToRent))
                .thenReturn("message");

        mockMvc.perform(post("/book_borrowers")
                        .with(user(borrower)).with(csrf())
                        .param("bookId", bookId.toString())
                        .param("ownerId", ownerId.toString())
                        .param("weeks", weeksToRent.toString()))
                .andExpectAll(
                        status().isCreated(),
                        content().string("message")
                )
                .andDo(print());

        verify(bookBorrowerService).borrowBookFromOwner(bookId, borrowerId, ownerId, weeksToRent);
    }

    @Test
    @DisplayName("extendRentingPeriod(..) throws bad request when extend cannot be done")
    void extendRentingPeriodShouldThrowBadRequest() throws Exception {
        User borrower = entries.get(0).getBorrower();
        Long bookId = 0L;
        when(bookBorrowerService.extendRentingPeriod(bookId, borrower.getId()))
                .thenThrow(ApiBadRequestException.class);

        mockMvc.perform(put("/book_borrowers")
                        .with(user(borrower))
                        .with(csrf())
                        .param("bookId", String.valueOf(bookId)))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(bookBorrowerService).extendRentingPeriod(bookId, borrower.getId());
    }

    @Test
    @DisplayName("extendRentingPeriod(..) returns ok when extend can be done")
    void extendRentingPeriodReturnOk() throws Exception {
        User borrower = entries.get(0).getBorrower();
        Long bookId = 2L;
        when(bookBorrowerService.extendRentingPeriod(bookId, borrower.getId()))
                .thenReturn("message");

        mockMvc.perform(put("/book_borrowers")
                        .with(user(borrower))
                        .with(csrf())
                        .param("bookId", String.valueOf(bookId)))
                .andExpectAll(
                        status().isOk(),
                        content().string("message")
                )
                .andDo(print());

        verify(bookBorrowerService).extendRentingPeriod(bookId, borrower.getId());
    }
}