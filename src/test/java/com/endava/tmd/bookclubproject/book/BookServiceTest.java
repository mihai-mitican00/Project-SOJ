package com.endava.tmd.bookclubproject.book;

import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static com.endava.tmd.bookclubproject.security.UserRoles.USER;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookOwnerRepository bookOwnerRepository;

    @Mock
    private BookBorrowerRepository bookBorrowerRepository;

    @InjectMocks
    private BookService bookService;

    private static List<Book> bookList;

    @BeforeAll
    static void setUp() {
        Book book1 = new Book("book1", "book1", "book1");
        Book book2 = new Book("book2", "book2", "book2");
        book1.setId(1L);
        book2.setId(2L);
        bookList = List.of(book1, book2);
    }

    @Test
    @DisplayName("getAllBooks() returns empty list when no books found")
    void getAllShouldReturnEmptyList() {
        when(bookRepository.findAll()).thenReturn(emptyList());

        List<Book> bookList = bookService.getAllBooks();
        assertThat(bookList).isEmpty();

        verify(bookRepository).findAll();
    }

    @Test
    @DisplayName("getAllBooks() returns book list when books found")
    void getAllShouldReturnBookList() {
        when(bookRepository.findAll()).thenReturn(bookList);

        //make it return actual book list
        List<Book> bookList = bookService.getAllBooks();
        assertThat(bookList).hasSize(2).isEqualTo(bookList);

        verify(bookRepository).findAll();
    }

    @Test
    @DisplayName("getBookById(..) returns empty optional when no book found")
    void getBookShouldReturnEmptyOptional() {
        when(bookRepository.findById(0L)).thenReturn(Optional.empty());

        Optional<Book> bookFound = bookService.getBookById(0L);
        assertThat(bookFound).isEmpty();

        verify(bookRepository).findById(0L);
    }

    @Test
    @DisplayName("getBookById(..) returns book when book found")
    void getBookShouldReturnBook() {
        Book book = bookList.get(0);
        Long bookId = book.getId();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        Optional<Book> bookFound = bookService.getBookById(bookId);
        assertThat(bookFound).isPresent().contains(book);

        verify(bookRepository).findById(bookId);
    }

    @Test
    @DisplayName("getBooksByTitleOrAuthor(..) returns empty list when no books found")
    void getBooksByTitleOrAuthorShouldReturnEmptyList() {
        String title = "notExistentTitle";
        String author = "";
        when(bookRepository.findAllByTitleOrAuthor(title, author)).thenReturn(emptyList());

        List<Book> booksByTitleOrAuthor = bookService.getBooksByTitleOrAuthor(title, author);
        assertThat(booksByTitleOrAuthor).isEmpty();

        verify(bookRepository).findAllByTitleOrAuthor(title, author);
    }

    @Test
    @DisplayName("getBooksByTitleOrAuthor(..) returns book list when books found")
    void getBooksByTitleOrAuthorShouldReturnBookList() {
        String title = "book1";
        String author = "book2";
        when(bookRepository.findAllByTitleOrAuthor(title, author)).thenReturn(bookList);

        List<Book> booksByTitleOrAuthor = bookService.getBooksByTitleOrAuthor(title, author);
        assertThat(booksByTitleOrAuthor).hasSize(2).isEqualTo(bookList);

        verify(bookRepository).findAllByTitleOrAuthor(title, author);
    }

    @Test
    @DisplayName("getBooksByTitleOrAuthor(..) returns empty list when no books found")
    void getAvailableBooksShouldReturnEmptyList() {
        when(bookRepository.findAvailableBooks()).thenReturn(emptyList());

        List<Book> availableBooks = bookService.getAllAvailableBooks();
        assertThat(availableBooks).isEmpty();

        verify(bookRepository).findAvailableBooks();
    }

    @Test
    @DisplayName("getBooksByTitleOrAuthor(..) returns book list when books found")
    void getAvailableBooksShouldReturnBookList() {
        List<Book> expectedBooks = bookList.subList(0, 1);
        when(bookRepository.findAvailableBooks()).thenReturn(expectedBooks);

        List<Book> actualBooks = bookService.getAllAvailableBooks();

        assertThat(actualBooks).hasSize(1).isEqualTo(expectedBooks);

        verify(bookRepository).findAvailableBooks();
    }

    @Test
    @DisplayName("getBookOwnersOfBook(..) returns empty list when no owners found")
    void getBookOwnersShouldReturnEmptyList() {
        when(bookOwnerRepository.findOwnersOfBook(0L)).thenReturn(emptyList());

        List<User> owners = bookService.getBookOwnersOfBook(0L);
        assertThat(owners).isEmpty();

        verify(bookOwnerRepository).findOwnersOfBook(0L);
    }

    @Test
    @DisplayName("getBookOwnersOfBook(..) returns user list when owners found")
    void getBookOwnersShouldReturnUserList() {
        Long id = bookList.get(0).getId();
        List<User> expectedOwners = List.of(
                new User("test", "test", "test", "test", "test", USER)
        );

        when(bookOwnerRepository.findOwnersOfBook(id)).thenReturn(expectedOwners);

        List<User> actualOwners = bookService.getBookOwnersOfBook(id);
        assertThat(actualOwners).hasSize(1).isEqualTo(expectedOwners);

        verify(bookOwnerRepository).findOwnersOfBook(id);
    }

    @Test
    @DisplayName("formatBook(..) returns formatted book")
    void formatBookShouldReturnFormattedBook() {
        Book book = bookList.get(0);
        Long bookId = book.getId();
        when(bookOwnerRepository.findOwnersIdsOfBook(bookId)).thenReturn(List.of(1L, 3L));

        String formattedBook = bookService.formatBook(book);
        assertThat(formattedBook)
                .isInstanceOf(String.class)
                .isNotEmpty();

        verify(bookOwnerRepository).findOwnersIdsOfBook(bookId);
        verify(bookBorrowerRepository).findByBookIdAndOwnerId(bookId, 1L);
        verify(bookBorrowerRepository).findByBookIdAndOwnerId(bookId, 3L);

    }

    @Test
    @DisplayName("formatBooksByTitleAndAuthor(..) returns formatted books")
    void formatByTitleAndAuthorShouldReturnStringOfBooks() {
        List<Book> booksByTitleOrAuthor = bookList;
        String formattedBooks = bookService.formatBooksByTitleOrAuthor(booksByTitleOrAuthor);

        assertThat(formattedBooks)
                .isInstanceOf(String.class)
                .isNotEmpty();

        verify(bookBorrowerRepository).findAllBorrowedBooks();
        verify(bookBorrowerRepository).findAllReturnDates();
    }

    @Test
    @DisplayName("formatAvailableBooks(..) returns formatted books")
    void formatAvailableBooksShouldReturnStringOfBooks() {
        List<Book> availableBooks = bookList;
        String formattedBooks = bookService.formatAvailableBooks(availableBooks);
        assertThat(formattedBooks)
                .isInstanceOf(String.class)
                .isNotEmpty();
    }
}