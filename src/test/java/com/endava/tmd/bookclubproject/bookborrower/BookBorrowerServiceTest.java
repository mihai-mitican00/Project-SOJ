package com.endava.tmd.bookclubproject.bookborrower;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.exception.ApiNotFoundException;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

import static com.endava.tmd.bookclubproject.security.UserRoles.USER;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith(MockitoExtension.class)
class BookBorrowerServiceTest {

    @Mock
    private BookBorrowerRepository bookBorrowerRepository;

    @Mock
    private BookOwnerRepository bookOwnerRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookBorrowerService bookBorrowerService;

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
    @DisplayName("getAllBookBorrowers() returns empty list when no entry found")
    void getAllShouldReturnEmptyList() {
        when(bookBorrowerRepository.findAll()).thenReturn(emptyList());

        List<BookBorrower> entriesFound = bookBorrowerService.getAllBookBorrowers();
        assertThat(entriesFound).isEmpty();

        verify(bookBorrowerRepository).findAll();
    }

    @Test
    @DisplayName("getAllBookBorrowers() returns empty list when no entry found")
    void getAllShouldReturnEntriesList() {
        when(bookBorrowerRepository.findAll()).thenReturn(entries);

        List<BookBorrower> entriesFound = bookBorrowerService.getAllBookBorrowers();
        assertThat(entriesFound).hasSize(2).isEqualTo(entries);

        verify(bookBorrowerRepository).findAll();
    }

    @Test
    @DisplayName("getBorrowsWhereUserGave(..) returns empty list when no entries found")
    void getBorrowsWhereUserGaveShouldReturnEmptyList() {
        Long ownerId = 0L;
        when(bookBorrowerRepository.findAllByOwnerId(ownerId)).thenReturn(emptyList());

        List<BookBorrower> borrowsList = bookBorrowerService.getBorrowsWhereUserGave(ownerId);
        assertThat(borrowsList).isEmpty();

        verify(bookBorrowerRepository).findAllByOwnerId(ownerId);
    }

    @Test
    @DisplayName("getBorrowsWhereUserGave(..) returns entry list when entries found")
    void getBorrowsWhereUserGaveShouldReturnEntryList() {
        Long ownerId = 2L;
        when(bookBorrowerRepository.findAllByOwnerId(ownerId)).thenReturn(entries);

        List<BookBorrower> borrowsList = bookBorrowerService.getBorrowsWhereUserGave(ownerId);
        assertThat(borrowsList).hasSize(2).isEqualTo(entries);

        verify(bookBorrowerRepository).findAllByOwnerId(ownerId);
    }

    @Test
    @DisplayName("getBorrowsWhereUserReceived() returns empty list when no entries found")
    void getBorrowsWhereUserReceivedShouldReturnEmptyList() {
        Long borrowerId = 0L;
        when(bookBorrowerRepository.findAllByBorrowerId(borrowerId)).thenReturn(emptyList());

        List<BookBorrower> borrowsList = bookBorrowerService.getBorrowsWhereUserReceived(borrowerId);
        assertThat(borrowsList).isEmpty();

        verify(bookBorrowerRepository).findAllByBorrowerId(borrowerId);
    }

    @Test
    @DisplayName("getBorrowsWhereUserReceived() returns entry list when entries found")
    void getBorrowsWhereUserReceivedShouldReturnEntryList() {
        Long borrowerId = 1L;
        when(bookBorrowerRepository.findAllByBorrowerId(borrowerId)).thenReturn(entries);

        List<BookBorrower> borrowsList = bookBorrowerService.getBorrowsWhereUserReceived(borrowerId);
        assertThat(borrowsList).hasSize(2).isEqualTo(entries);

        verify(bookBorrowerRepository).findAllByBorrowerId(borrowerId);
    }

    @Test
    @DisplayName("formatBooksThatUserGave(..) returns formatted books as string")
    void formatBooksThatUserGaveShouldReturnFormattedBooks() {
        String formattedBooks = bookBorrowerService.formatBooksThatUserGave(entries);

        System.out.println(formattedBooks);
        assertThat(formattedBooks)
                .isInstanceOf(String.class)
                .isNotEmpty();
    }

    @Test
    @DisplayName("formatBooksThatUserReceived(..) returns formatted books as string")
    void formatBooksThatUserReceivedShouldReturnFormattedBooks() {
        String formattedBooks = bookBorrowerService.formatBooksThatUserReceived(entries);
        System.out.println(formattedBooks);
        assertThat(formattedBooks)
                .isInstanceOf(String.class)
                .isNotEmpty();
    }

    @Test
    @DisplayName("borrowBookFromOwner(..) throws not found when data is invalid")
    void borrowBookShouldThrowNotFound() {
        Long bookId = 0L;
        Long ownerId = 0L;
        Long borrowerId = 3L;
        Long weeksToRent = 12L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        when(userRepository.findById(borrowerId)).thenReturn(Optional.empty());
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        String errorMessage = "Invalid params, can not proceed with the borrow!";
        assertThatThrownBy(() -> bookBorrowerService.borrowBookFromOwner(bookId, borrowerId, ownerId, weeksToRent))
                .isInstanceOf(ApiNotFoundException.class)
                .hasMessage(errorMessage);

        verify(bookRepository).findById(bookId);
        verify(userRepository).findById(borrowerId);
        verify(userRepository).findById(ownerId);
    }

    @Test
    @DisplayName("borrowBookFromOwner(..) throws bad request when borrower already have the book")
    void borrowBookShouldThrowBadRequestBorrowerOwnTheBook() {
        Book book = mock(Book.class);
        User owner = mock(User.class);
        User borrower = mock(User.class);
        BookOwner bookOwner = mock(BookOwner.class);
        Long bookId = 1L;
        Long ownerId = 1L; //same user
        Long borrowerId = 1L; //same user
        Long weeksToRent = 3L;


        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, borrowerId)).thenReturn(Optional.of(bookOwner));

        String errorMessage = "The user cannot rent a book that himself owns.";
        assertThatThrownBy(() -> bookBorrowerService.borrowBookFromOwner(bookId, borrowerId, ownerId, weeksToRent))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(bookRepository).findById(bookId);
        verify(userRepository, times(2)).findById(ownerId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, borrowerId);
    }

    @Test
    @DisplayName("borrowBookFromOwner(..) throws bad request when book does not belong to given owner")
    void borrowBookShouldThrowBadRequestWrongOwnership() {
        Book book = mock(Book.class);
        User owner = mock(User.class);
        User borrower = mock(User.class);

        Long bookId = 1L;
        Long ownerId = 3L;
        Long borrowerId = 1L;
        Long weeksToRent = 3L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        when(bookOwnerRepository.findByBookIdAndUserId(bookId, borrowerId)).thenReturn(Optional.empty());
        //There is no entry for the given owner id for respective book
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId)).thenReturn(Optional.empty());

        String errorMessage = "The book does not belong to given owner.";
        assertThatThrownBy(() -> bookBorrowerService.borrowBookFromOwner(bookId, borrowerId, ownerId, weeksToRent))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(bookRepository).findById(bookId);
        verify(userRepository).findById(borrowerId);
        verify(userRepository).findById(ownerId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, borrowerId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, ownerId);
    }

    @Test
    @DisplayName("borrowBookFromOwner(..) throws bad request when given book is already borrowed")
    void borrowBookShouldThrowBadRequestAlreadyBorrowed() {
        Book book = mock(Book.class);
        User owner = mock(User.class);
        User borrower = mock(User.class);
        BookOwner bookOwner = mock(BookOwner.class);
        BookBorrower bookBorrower = mock(BookBorrower.class);

        Long bookId = 1L;
        Long ownerId = 2L;
        Long borrowerId = 1L;
        Long weeksToRent = 3L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        //owner not the same as borrower
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, borrowerId)).thenReturn(Optional.empty());
        //ownership is true
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId)).thenReturn(Optional.of(bookOwner));
        //book is already borrowed
        when(bookBorrowerRepository.findByBookIdAndOwnerId(bookId, ownerId)).thenReturn(Optional.of(bookBorrower));

        String errorMessage = "The given book is already borrowed at the moment.";
        assertThatThrownBy(() -> bookBorrowerService.borrowBookFromOwner(bookId, borrowerId, ownerId, weeksToRent))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(bookRepository).findById(bookId);
        verify(userRepository).findById(borrowerId);
        verify(userRepository).findById(ownerId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, borrowerId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, ownerId);
        verify(bookBorrowerRepository).findByBookIdAndOwnerId(bookId, ownerId);
    }


    @Test
    @DisplayName("borrowBookFromOwner(..) returns message when borrow is done")
    void borrowBookShouldReturnSuccessMessage() {
        Book book = entries.get(0).getBook();
        User owner = mock(User.class);
        User borrower = entries.get(0).getBorrower();
        BookOwner bookOwner = mock(BookOwner.class);
        BookBorrower bookBorrower = new BookBorrower(book, borrower, 2L, 3L);

        Long bookId = 1L;
        Long ownerId = 2L;
        Long borrowerId = 1L;
        Long weeksToRent = 3L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        //owner not the same as borrower
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, borrowerId)).thenReturn(Optional.empty());
        //ownership is true
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId)).thenReturn(Optional.of(bookOwner));
        //book is not already borrowed
        when(bookBorrowerRepository.findByBookIdAndOwnerId(bookId, ownerId)).thenReturn(Optional.empty());

        String expectedMessage = String.format("Hello %s!%nYou just borrowed \"%s\".%nHappy reading!",
                borrower.getUsername(),
                book.getTitle());

        String actualMessage = bookBorrowerService.borrowBookFromOwner(bookId, borrowerId, ownerId, weeksToRent);

        assertThat(actualMessage)
                .isNotEmpty()
                .isEqualTo(expectedMessage);

        verify(bookRepository).findById(bookId);
        verify(userRepository).findById(borrowerId);
        verify(userRepository).findById(ownerId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, borrowerId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, ownerId);
        verify(bookBorrowerRepository).findByBookIdAndOwnerId(bookId, ownerId);
        verify(bookBorrowerRepository).save(bookBorrower);
    }

    @Test
    @DisplayName("extendRentingPeriod(..) throws not found when borrow not found")
    void extendRentingPeriodShouldThrowNotFound() {
        Long bookId = 1L;
        Long borrowerId = 0L;
        when(bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, borrowerId))
                .thenReturn(Optional.empty());

        String errorMessage = "The borrow with given id's does not exist.";
        assertThatThrownBy(() -> bookBorrowerService.extendRentingPeriod(bookId, borrowerId))
                .isInstanceOf(ApiNotFoundException.class)
                .hasMessage(errorMessage);

        verify(bookBorrowerRepository).findByBookIdAndBorrowerId(bookId, borrowerId);
    }

    @Test
    @DisplayName("extendRentingPeriod(..) throws bad request when return date limit passed")
    void extendRentingPeriodShouldThrowBadRequest() {
        Book book = mock(Book.class);
        User borrower = mock(User.class);
        BookBorrower bookBorrower = new BookBorrower(book, borrower, 2L, 5L);
        Long bookId = 1L;
        Long borrowerId = 1L;
        when(bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, borrowerId))
                .thenReturn(Optional.of(bookBorrower));

        String errorMessage = "This borrow return date cannot be extended anymore!";
        assertThatThrownBy(() -> bookBorrowerService.extendRentingPeriod(bookId, borrowerId))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(bookBorrowerRepository).findByBookIdAndBorrowerId(bookId, borrowerId);
    }

    @Test
    @DisplayName("extendRentingPeriod(..) returns message when extend is success")
    void extendRentingPeriodShouldReturnMessage() {
        Book book = entries.get(0).getBook();
        User borrower = mock(User.class);
        BookBorrower bookBorrower = new BookBorrower(book, borrower, 2L, 4L);
        Long bookId = 1L;
        Long borrowerId = 1L;
        when(bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, borrowerId))
                .thenReturn(Optional.of(bookBorrower));

        String expectedMessage = String.format("You just extended renting period for book \"%s\" for another week",
                bookBorrower.getBook().getTitle());
        String actualMessage = bookBorrowerService.extendRentingPeriod(bookId, borrowerId);
        assertThat(actualMessage)
                .isNotEmpty()
                .isEqualTo(expectedMessage);

        verify(bookBorrowerRepository).findByBookIdAndBorrowerId(bookId, borrowerId);
        verify(bookBorrowerRepository).save(bookBorrower);
    }


}