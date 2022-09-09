package com.endava.tmd.bookclubproject.book;

import com.endava.tmd.bookclubproject.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMapAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@WebMvcTest(BookController.class)
@WithMockUser
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private static List<Book> bookList;

    @BeforeAll
    static void setUp() {
        Book book1 = new Book("Title1", "Author1", "I");
        Book book2 = new Book("Title2", "Author2", "II");
        Book book3 = new Book("Title3", "Author3", " III");
        book1.setId(1L);
        book2.setId(2L);
        book3.setId(3L);

        bookList = List.of(book1, book2, book3);
    }

    @Test
    @DisplayName("getAllBooks() returns no content when no books found")
    void getAllShouldReturnNoContent() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/books"))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(bookService).getAllBooks();
    }

    @Test
    @DisplayName("getAllBooks() returns books when books found")
    void getAllShouldReturnOk() throws Exception {
        when(bookService.getAllBooks()).thenReturn(bookList);


        String expectedBooks = objectMapper.writeValueAsString(bookList);
        mockMvc.perform(get("/books"))
                .andExpectAll(
                        status().isOk(),
                        content().json(expectedBooks, true)
                )
                .andDo(print());

        verify(bookService).getAllBooks();
    }

    @Test
    @DisplayName("getBookById(..) throws not found when no book found")
    void getBookShouldReturnNotFound() throws Exception {
        Long id = 0L;
        when(bookService.getBookById(id)).thenReturn(Optional.empty());

        String errorMessage = String.format("There is no book with id %d.", id);
        mockMvc.perform(get("/books/{id}", id))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message", is(errorMessage))
                )
                .andDo(print());

        verify(bookService).getBookById(id);
    }

    @Test
    @DisplayName("getBookById(..) returns book when book found")
    void getBookShouldReturnOk() throws Exception {
        Long id = 1L;
        Book testBook = bookList.get(0);
        when(bookService.getBookById(id)).thenReturn(Optional.of(testBook));

        String expectedBook = objectMapper.writeValueAsString(testBook);
        mockMvc.perform(get("/books/{id}", id))
                .andExpectAll(
                        status().isOk(),
                        content().json(expectedBook, true)
                )
                .andDo(print());

        verify(bookService).getBookById(id);
    }

    @Test
    @DisplayName("getAllAvailableBooks() returns no content when no books found")
    void getAvailableBooksShouldReturnNoContent() throws Exception {
        when(bookService.getAllAvailableBooks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/books/availableBooks"))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(bookService).getAllAvailableBooks();
    }

    @Test
    @DisplayName("getAllAvailableBooks() returns available books when books found")
    void getAvailableBooksShouldReturnOk() throws Exception {
        List<Book> availableBooks = List.of(bookList.get(1), bookList.get(2));
        when(bookService.getAllAvailableBooks()).thenReturn(availableBooks);
        when(bookService.formatAvailableBooks(availableBooks)).thenReturn("formattedBooks");

        mockMvc.perform(get("/books/availableBooks"))
                .andExpectAll(
                        status().isOk(),
                        content().string("formattedBooks")
                )
                .andDo(print());

        verify(bookService).getAllAvailableBooks();
        verify(bookService).formatAvailableBooks(availableBooks);
    }

    @Test
    @DisplayName("getBooksByTitleOrAuthor(..) returns no content when no books found")
    void getBooksByTitleOrAuthorShouldReturnNoContent() throws Exception {
        String author = "notExistentAuthor";
        when(bookService.getBooksByTitleOrAuthor(null, author))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/books/TitleOrAuthor")
                        .param("author", author)
                )
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(bookService).getBooksByTitleOrAuthor(null, author);
    }

    @Test
    @DisplayName("getBooksByTitleOrAuthor(..) returns books when books found")
    void getBooksByTitleOrAuthorShouldReturnOk() throws Exception {
        String title = "notExistentTitle";
        String author = "Author1";
        List<Book> booksByTitleOrAuthor = bookList.subList(0, 1);
        when(bookService.getBooksByTitleOrAuthor(title, author))
                .thenReturn(booksByTitleOrAuthor);
        when(bookService.formatBooksByTitleOrAuthor(booksByTitleOrAuthor)).thenReturn("formattedBooks");

        mockMvc.perform(get("/books/TitleOrAuthor")
                        .param("title", title)
                        .param("author", author)
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("formattedBooks")
                )
                .andDo(print());

        verify(bookService).getBooksByTitleOrAuthor(title, author);
        verify(bookService).formatBooksByTitleOrAuthor(booksByTitleOrAuthor);
    }

    @Test
    @DisplayName("getBookOwnersOfBook(..) throws not found when no book found")
    void getBookOwnersShouldThrowNotFound() throws Exception {
        Long id = 0L;
        when(bookService.getBookById(id)).thenReturn(Optional.empty());

        String errorMessage = String.format("There is no book with id %d.", id);
        mockMvc.perform(get("/books/BookOwners")
                        .param("bookId", String.valueOf(id))
                )
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(APPLICATION_JSON),
                        jsonPath("$.message", is(errorMessage))
                )
                .andDo(print());

        verify(bookService).getBookById(id);
    }

    @Test
    @DisplayName("getBookOwnersOfBook(..) returns book owners when book found")
    void getBookOwnersReturnOk() throws Exception {
        Book bookFound = bookList.get(1);
        Long id = bookFound.getId();
        List<User> bookOwners = List.of(new User());
        when(bookService.getBookById(id)).thenReturn(Optional.of(bookFound));
        when(bookService.getBookOwnersOfBook(id)).thenReturn(bookOwners);

        String expectedOwners = objectMapper.writeValueAsString(bookOwners);
        mockMvc.perform(get("/books/BookOwners")
                        .param("bookId", String.valueOf(id))
                )
                .andExpectAll(
                        status().isOk(),
                        content().json(expectedOwners, true)
                )
                .andDo(print());

        verify(bookService).getBookById(id);
        verify(bookService).getBookOwnersOfBook(id);
    }
}
