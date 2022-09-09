package com.endava.tmd.bookclubproject.waitinglist;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
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
class WaitingListServiceTest {

    @Mock
    private WaitingListRepository waitingListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookOwnerRepository bookOwnerRepository;

    @Mock
    private BookBorrowerRepository bookBorrowerRepository;

    @InjectMocks
    private WaitingListService waitingListService;

    private static List<WaitingList> entries;

    private static User owner, borrower;

    private static Book book;

    @BeforeAll
    static void setUp() {
        book = new Book("book1", "book1", "book1");
        book.setId(1L);

        owner = new User("owner", "owner", "owner", "owner", "owner", USER);
        borrower = new User(
                "borrower", "borrower", "borrower", "borrower", "borrower", USER
        );
        owner.setId(1L);
        borrower.setId(2L);

        WaitingList entry1 = new WaitingList(1L, 1L, 2L);
        WaitingList entry2 = new WaitingList(2L, 2L, 1L);
        entry1.setId(1L);
        entry2.setId(2L);

        entries = List.of(entry1, entry2);
    }

    @Test
    @DisplayName("getAllOnWaitingList(..) returns empty list when no entries found")
    void getAllShouldReturnEmptyList() {
        when(waitingListRepository.findAll()).thenReturn(emptyList());

        List<WaitingList> entries = waitingListService.getAllOnWaitingList();
        assertThat(entries).isEmpty();

        verify(waitingListRepository).findAll();
    }

    @Test
    @DisplayName("getAllOnWaitingList(..) returns entry list when entries found")
    void getAllShouldReturnEntryList() {
        when(waitingListRepository.findAll()).thenReturn(entries);

        List<WaitingList> entriesFound = waitingListService.getAllOnWaitingList();
        assertThat(entriesFound).hasSize(2).isEqualTo(entries);

        verify(waitingListRepository).findAll();
    }

    @Test
    @DisplayName("AddUserOnList(..) throws bad request whan invalid data")
    void addUserOnListShouldThrowBadRequestInvalidData() {
        when(bookOwnerRepository.findByBookIdAndUserId(0L, 0L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(0L)).thenReturn(Optional.empty());

        String errorMessage = "Invalid data, some objects do not exist.";
        assertThatThrownBy(() -> waitingListService.addUserOnList(0L, 0L, 0L))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(bookOwnerRepository).findByBookIdAndUserId(0L, 0L);
        verify(userRepository).findById(0L);
    }

    @Test
    @DisplayName("AddUserOnList(..) throws bad request when book is not borrowed")
    void addUserOnListShouldThrowBadRequestBookNotBorrowed() {
        Long bookId = book.getId();
        Long ownerId = owner.getId();
        Long borrowerId = borrower.getId();
        BookOwner bookOwnerEntry = mock(BookOwner.class);

        //return book with owner that exist
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId))
                .thenReturn(Optional.of(bookOwnerEntry));
        //returns existent candidate borrower
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        //returns empty, meaning borrow on that book and owner was not made
        when(bookBorrowerRepository.findByBookIdAndOwnerId(bookId, ownerId))
                .thenReturn(Optional.empty());

        String errorMessage = "You cannot add yourself on the waiting list for a book that is not already rented!";
        assertThatThrownBy(() -> waitingListService.addUserOnList(bookId, ownerId, borrowerId))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, ownerId);
        verify(userRepository).findById(borrowerId);
        verify(bookBorrowerRepository).findByBookIdAndOwnerId(bookId, ownerId);
    }

    @Test
    @DisplayName("AddUserOnList(..) throws bad request when borrower already owns the book")
    void addUserOnListShouldThrowBadRequestBorrowerOwnsBook() {
        Long bookId = book.getId();
        Long ownerId = borrower.getId();
        Long borrowerId = borrower.getId();

        BookBorrower bookBorrowerEntry = mock(BookBorrower.class);
        BookOwner bookOwnerEntry = mock(BookOwner.class);

        //return book with owner that exist
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId))
                .thenReturn(Optional.of(bookOwnerEntry));

        //returns existent borrower
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        //returns entry, meaning borrow on that book is made
        when(bookBorrowerRepository.findByBookIdAndOwnerId(bookId, ownerId))
                .thenReturn(Optional.of(bookBorrowerEntry));
        //borrower has the book
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, borrowerId))
                .thenReturn(Optional.of(bookOwnerEntry));

        String errorMessage = "User cannot be added on waiting list for his own book.";
        assertThatThrownBy(() -> waitingListService.addUserOnList(bookId, ownerId, borrowerId))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(bookOwnerRepository, times(2)).findByBookIdAndUserId(bookId, ownerId);
        verify(userRepository).findById(borrowerId);
        verify(bookBorrowerRepository).findByBookIdAndOwnerId(bookId, ownerId);
    }

    @Test
    @DisplayName("AddUserOnList(..) throws bad request when borrower already borrowed the book")
    void addUserOnListShouldThrowBadRequestBorrowerIsRentingTheBook() {
        Long bookId = book.getId();
        Long ownerId = owner.getId();
        Long borrowerId = borrower.getId();

        BookBorrower bookBorrowerEntry = mock(BookBorrower.class);
        BookOwner bookOwnerEntry = mock(BookOwner.class);

        //return book with owner that exist
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId))
                .thenReturn(Optional.of(bookOwnerEntry));

        //returns existent borrower
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        //returns empty, meaning borrow on that book and owner was not made
        when(bookBorrowerRepository.findByBookIdAndOwnerId(bookId, ownerId))
                .thenReturn(Optional.of(bookBorrowerEntry));
        //borrower does not have the book
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, borrowerId))
                .thenReturn(Optional.empty());

        //borrower currently is the one that is renting the book
        when(bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, borrowerId))
                .thenReturn(Optional.of(bookBorrowerEntry));

        String errorMessage = String.format("User having id %d is already renting book with id %d", borrowerId, bookId);
        assertThatThrownBy(() -> waitingListService.addUserOnList(bookId, ownerId, borrowerId))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, ownerId);
        verify(userRepository).findById(borrowerId);
        verify(bookBorrowerRepository).findByBookIdAndOwnerId(bookId, ownerId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, borrowerId);
        verify(bookBorrowerRepository).findByBookIdAndBorrowerId(bookId, borrowerId);
    }

    @Test
    @DisplayName("AddUserOnList(..) throws bad request when borrower already waits for this book")
    void addUserOnListShouldThrowBadRequestBorrowerIsAlreadyWaitingForBook() {
        Long bookId = book.getId();
        Long ownerId = owner.getId();
        Long borrowerId = borrower.getId();

        BookBorrower bookBorrowerEntry = mock(BookBorrower.class);
        BookOwner bookOwnerEntry = mock(BookOwner.class);
        WaitingList waitingList = mock(WaitingList.class);

        //return book with owner that exist
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId))
                .thenReturn(Optional.of(bookOwnerEntry));

        //returns existent borrower
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        //returns empty, meaning borrow on that book and owner was not made
        when(bookBorrowerRepository.findByBookIdAndOwnerId(bookId, ownerId))
                .thenReturn(Optional.of(bookBorrowerEntry));
        //borrower does not have the book
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, borrowerId))
                .thenReturn(Optional.empty());

        //borrower is not currently the one that is renting the book
        when(bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, borrowerId))
                .thenReturn(Optional.empty());

        //borrower already waits for the book
        when(waitingListRepository.findByBookIdAndUserId(bookId, borrowerId))
                .thenReturn(Optional.of(waitingList));

        String errorMessage = String.format(
                "User with id %d already added himself on waiting list for book with id %d",
                borrowerId,
                bookId);
        assertThatThrownBy(() -> waitingListService.addUserOnList(bookId, ownerId, borrowerId))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, ownerId);
        verify(userRepository).findById(borrowerId);
        verify(bookBorrowerRepository).findByBookIdAndOwnerId(bookId, ownerId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, borrowerId);
        verify(bookBorrowerRepository).findByBookIdAndBorrowerId(bookId, borrowerId);
        verify(waitingListRepository).findByBookIdAndUserId(bookId, borrowerId);
    }

    @Test
    @DisplayName("AddUserOnList(..) adds user on waiting list when success")
    void addUserOnListShouldAddUserOnWaitingList() {
        Long bookId = book.getId();
        Long ownerId = owner.getId();
        Long borrowerId = borrower.getId();

        BookBorrower bookBorrowerEntry = mock(BookBorrower.class);
        BookOwner bookOwnerEntry = mock(BookOwner.class);

        //return book with owner that exist
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId))
                .thenReturn(Optional.of(bookOwnerEntry));

        //returns existent borrower
        when(userRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));

        //returns empty, meaning borrow on that book and owner was not made
        when(bookBorrowerRepository.findByBookIdAndOwnerId(bookId, ownerId))
                .thenReturn(Optional.of(bookBorrowerEntry));
        //borrower does not have the book
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, borrowerId))
                .thenReturn(Optional.empty());

        //borrower is not currently the one that is renting the book
        when(bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, borrowerId))
                .thenReturn(Optional.empty());

        //borrower is not already waiting for the book
        when(waitingListRepository.findByBookIdAndUserId(bookId, borrowerId))
                .thenReturn(Optional.empty());

        WaitingList waitingList = new WaitingList(bookId, ownerId, borrowerId);
        waitingListService.addUserOnList(bookId, ownerId, borrowerId);

        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, ownerId);
        verify(userRepository).findById(borrowerId);
        verify(bookBorrowerRepository).findByBookIdAndOwnerId(bookId, ownerId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, borrowerId);
        verify(bookBorrowerRepository).findByBookIdAndBorrowerId(bookId, borrowerId);
        verify(waitingListRepository).findByBookIdAndUserId(bookId, borrowerId);
        verify(waitingListRepository).save(waitingList);
    }
}