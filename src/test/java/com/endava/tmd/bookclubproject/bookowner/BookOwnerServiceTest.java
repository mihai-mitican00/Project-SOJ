package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerService;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.exception.ApiNotFoundException;
import com.endava.tmd.bookclubproject.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import static com.endava.tmd.bookclubproject.security.UserRoles.USER;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith(MockitoExtension.class)
class BookOwnerServiceTest {

    @Mock
    private BookOwnerRepository bookOwnerRepository;

    @Mock
    private BookBorrowerRepository bookBorrowerRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookOwnerService bookOwnerService;

    private static List<BookOwner> entries;

    @BeforeAll
    static void setUp() {
        Book book1 = new Book("book1", "book1", "book1");
        Book book2 = new Book("book2", "book2", "book2");
        book1.setId(1L);
        book2.setId(2L);

        User owner1 = new User("owner1", "owner1", "owner1", "owner1", "owner1", USER);
        User owner2 = new User("owner2", "owner2", "owner2", "owner2", "owner2", USER);
        owner1.setId(1L);
        owner2.setId(2L);

        BookOwner entry1 = new BookOwner(book1, owner1);
        BookOwner entry2 = new BookOwner(book2, owner2);

        entries = List.of(entry1, entry2);
    }

    @Test
    @DisplayName("getAllBookOwners() returns empty list when no entry found")
    void getAllShouldReturnEmptyList() {
        when(bookOwnerRepository.findAll()).thenReturn(emptyList());

        List<BookOwner> entriesFound = bookOwnerService.getAllBookOwners();
        assertThat(entriesFound).isEmpty();

        verify(bookOwnerRepository).findAll();
    }

    @Test
    @DisplayName("getAllBookOwners() returns entries list when entries found")
    void getAllShouldReturnEntryList() {
        when(bookOwnerRepository.findAll()).thenReturn(entries);

        List<BookOwner> entriesFound = bookOwnerService.getAllBookOwners();
        assertThat(entriesFound).hasSize(2).isEqualTo(entries);

        verify(bookOwnerRepository).findAll();
    }

    @Test
    @DisplayName("addBookAsUser(..) throws bad request when data is invalid")
    void addBookShouldThrowBadRequestInvalidData() {
        Book invalidBook = new Book("title", "", "I");
        User user = entries.get(0).getUser();

        String errorMessage = "Book has incomplete data, enter something on all fields!";
        assertThatThrownBy(() -> bookOwnerService.addBookAsUser(user, invalidBook))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("addBookAsUser(..) throws bad request when user adds book he previously added")
    void addBookShouldThrowBadRequestAddSameBookTwice() {
        User user = entries.get(0).getUser();
        Book book = entries.get(0).getBook();
        String title = book.getTitle();
        String author = book.getAuthor();
        String edition = book.getEdition();

        when(bookRepository.findByTitleAndAuthorAndEdition(title, author, edition))
                .thenReturn(Optional.of(book));
        when(bookOwnerRepository.findBooksOfUser(user.getId())).thenReturn(List.of(book));

        String errorMessage = "You already added this book.";
        assertThatThrownBy(() -> bookOwnerService.addBookAsUser(user, book))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(bookRepository).findByTitleAndAuthorAndEdition(title, author, edition);
        verify(bookOwnerRepository).findBooksOfUser(user.getId());
    }

    @Test
    @DisplayName("addBookAsUser(..) adds book when book was added by others")
    void addBookAsUserShouldAddBookIfWasAddedByOthers() {
        User user = entries.get(0).getUser();
        Book book = entries.get(0).getBook();
        String title = book.getTitle();
        String author = book.getAuthor();
        String edition = book.getEdition();

        BookOwner bookOwner = entries.get(0);

        when(bookRepository.findByTitleAndAuthorAndEdition(title, author, edition))
                .thenReturn(Optional.of(book));
        when(bookOwnerRepository.findBooksOfUser(user.getId())).thenReturn(emptyList());

        bookOwnerService.addBookAsUser(user, book);


        verify(bookRepository).findByTitleAndAuthorAndEdition(title, author, edition);
        verify(bookOwnerRepository).findBooksOfUser(user.getId());
        verify(bookOwnerRepository).save(bookOwner);
    }

    @Test
    @DisplayName("addBookAsUser(..) adds book when book was not added by others")
    void addBookAsUserShouldAddBookIfNoOneAddedIt() {
        User user = entries.get(0).getUser();
        Book book = entries.get(0).getBook();
        String title = book.getTitle();
        String author = book.getAuthor();
        String edition = book.getEdition();

        BookOwner bookOwner = entries.get(0);

        when(bookRepository.findByTitleAndAuthorAndEdition(title, author, edition))
                .thenReturn(Optional.empty());

        bookOwnerService.addBookAsUser(user, book);


        verify(bookRepository).findByTitleAndAuthorAndEdition(title, author, edition);
        verify(bookRepository).save(book);
        verify(bookOwnerRepository).save(bookOwner);
    }

    @Test
    @DisplayName("deleteBookAsUser(..) throws not found when no book found")
    void deleteBookShouldThrowNotFound() {
        Long bookId = 0L;
        Long ownerId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        String errorMessage = String.format("There is no book with id %d", bookId);
        assertThatThrownBy(() -> bookOwnerService.deleteBookAsUser(bookId, ownerId))
                .isInstanceOf(ApiNotFoundException.class)
                .hasMessage(errorMessage);

        verify(bookRepository).findById(bookId);
    }

    @Test
    @DisplayName("deleteBookAsUser(..) throws bad request when book doesn't belong to user")
    void deleteBookShouldThrowBadRequest() {
        Book book = mock(Book.class);
        Long bookId = 2L;
        Long ownerId = 1L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId)).thenReturn(Optional.empty());

        String errorMessage = "Given book does not belong to you.";
        assertThatThrownBy(() -> bookOwnerService.deleteBookAsUser(bookId, ownerId))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(bookRepository).findById(bookId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, ownerId);
    }

    @Test
    @DisplayName("deleteBookAsUser(..) deletes book when others have this book too")
    void deleteBookShouldDeleteWhenOthersHaveItToo() {
        BookOwner entry = entries.get(0);
        Book book = entry.getBook();
        User owner = entry.getUser();
        Long bookId = book.getId(); //1L
        Long ownerId = owner.getId(); //1L

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId)).thenReturn(Optional.of(entry));
        //find more owners that have that book
        when(bookOwnerRepository.findAllByBookId(bookId)).thenReturn(List.of(new BookOwner()));

        bookOwnerService.deleteBookAsUser(bookId, ownerId);

        verify(bookRepository).findById(bookId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, ownerId);
        verify(bookBorrowerRepository).deleteByBookIdAndOwnerId(bookId, ownerId);
        verify(bookOwnerRepository).delete(entry);
        //check that book is not deleted completely, because others still own it
        verify(bookRepository, never()).delete(book);
    }

    @Test
    @DisplayName("deleteBookAsUser(..) deletes book when only this user have it")
    void deleteBookShouldDeleteWhenUnique() {
        BookOwner entry = entries.get(0);
        Book book = entry.getBook();
        User owner = entry.getUser();
        Long bookId = book.getId(); //1L
        Long ownerId = owner.getId(); //1L

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId)).thenReturn(Optional.of(entry));
        //find no more owners that have that book
        when(bookOwnerRepository.findAllByBookId(bookId)).thenReturn(emptyList());

        bookOwnerService.deleteBookAsUser(bookId, ownerId);

        verify(bookRepository).findById(bookId);
        verify(bookOwnerRepository).findByBookIdAndUserId(bookId, ownerId);
        verify(bookBorrowerRepository).deleteByBookIdAndOwnerId(bookId, ownerId);
        verify(bookOwnerRepository).delete(entry);
        verify(bookRepository).delete(book);
    }
}