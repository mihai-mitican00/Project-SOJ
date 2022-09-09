package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.exception.ApiNotFoundException;
import com.endava.tmd.bookclubproject.registration.token.ConfirmationToken;
import com.endava.tmd.bookclubproject.registration.token.ConfirmationTokenService;
import com.endava.tmd.bookclubproject.waitinglist.WaitingListRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.endava.tmd.bookclubproject.security.UserRoles.USER;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookOwnerRepository bookOwnerRepository;

    @Mock
    private BookBorrowerRepository bookBorrowerRepository;

    @Mock
    private WaitingListRepository waitingListRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ConfirmationTokenService confirmationTokenService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test", "test", "test", "test", "test@mail", USER);
        testUser.setId(1L);
        testUser.setEnabled(false);
    }

    @Test
    @DisplayName("loadByUsername(..) throws not found when user not found by email")
    void loadByUsernameShouldThrowNotFound() {
        String email = testUser.getEmail();
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        String errorMessage = String.format("User with email %s not found", email);
        assertThatThrownBy(() -> userService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(errorMessage);

        verify(userRepository).findUserByEmail(email);
    }

    @Test
    @DisplayName("loadByUsername(..) returns user when user found by email")
    void loadByUsernameShouldReturnUser() {
        String email = testUser.getEmail();
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(testUser));

        User userFound = (User) userService.loadUserByUsername(email);
        assertThat(userFound).isEqualTo(testUser);

        verify(userRepository).findUserByEmail(email);
    }

    @Test
    @DisplayName("getAllUsers(..) returns empty list when no user found")
    void getAllShouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(emptyList());

        List<User> usersFound = userService.getAllUsers();
        assertThat(usersFound).isEmpty();

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getAllUsers(..) returns user list when user found")
    void getAllShouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<User> usersFound = userService.getAllUsers();
        assertThat(usersFound).hasSize(1).contains(testUser);

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getUserById(..) returns empty optional when no user found")
    void getUserShouldReturnEmptyOptional() {
        when(userRepository.findById(0L)).thenReturn(Optional.empty());

        Optional<User> userFound = userService.getUserById(0L);
        assertThat(userFound).isEmpty();

        verify(userRepository).findById(0L);
    }

    @Test
    @DisplayName("getUserById(..) returns user when user found")
    void getUserShouldReturnUser() {
        Long id = testUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(testUser));

        Optional<User> userFound = userService.getUserById(id);
        assertThat(userFound).contains(testUser);

        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("getBooksOfUser(..) returns empty list when user has no books")
    void getBooksOfUserShouldReturnEmptyList() {
        when(bookOwnerRepository.findBooksOfUser(0L)).thenReturn(emptyList());

        List<Book> usersFound = userService.getBooksOfUser(0L);
        assertThat(usersFound).isEmpty();

        verify(bookOwnerRepository).findBooksOfUser(0L);
    }

    @Test
    @DisplayName("getBooksOfUser(..) returns list of books when user has books")
    void getBooksOfUserShouldReturnBookList() {
        Long id = testUser.getId();
        List<Book> booksOfUser = List.of(
                new Book("book1", "book1", "book1"),
                new Book("book2", "book2", "book2")
        );

        when(bookOwnerRepository.findBooksOfUser(id)).thenReturn(booksOfUser);

        List<Book> usersFound = userService.getBooksOfUser(id);
        assertThat(usersFound).hasSize(2).isEqualTo(booksOfUser);

        verify(bookOwnerRepository).findBooksOfUser(id);
    }

    @Test
    @DisplayName("enableUserAccount(..) throws not found when user not found")
    void enableAccountShouldThrowNotFound() {
        String email = "notExistent";
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.enableUserAccount(email))
                .isInstanceOf(ApiNotFoundException.class)
                .hasMessage("User not found!");

        verify(userRepository).findUserByEmail(email);
    }

    @Test
    @DisplayName("enableUserAccount(..) sets user as enabled when user found")
    void enableAccountShouldSetUserEnabled() {
        String email = testUser.getEmail();

        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(testUser));

        userService.enableUserAccount(email);
        boolean enabled = testUser.isEnabled();
        assertThat(enabled).isTrue();

        verify(userRepository).findUserByEmail(email);
    }

    @Test
    @DisplayName("registerUser(..) throws bad request when data is invalid")
    void registerUserShouldThrowBadRequestInvalidData() {
        testUser.setEmail("");
        Optional<User> user = Optional.of(testUser);

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage("Details for user are not complete!");

    }

    @Test
    @DisplayName("registerUser(..) throws bad request when email is already used")
    void registerUserShouldThrowBadRequestEmailAlreadyUsed() {
        testUser.setEnabled(true);
        String email = testUser.getEmail();
        Optional<User> userByEmail = Optional.of(testUser);

        when(userRepository.findUserByEmail(email)).thenReturn(userByEmail);

        String errorMessage = String.format("User with email %s is already registered!", email);
        assertThatThrownBy(() -> userService.registerUser(userByEmail))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(errorMessage);

        verify(userRepository).findUserByEmail(email);
    }

    @Test
    @DisplayName("registerUser(..) resend confirmation token if user is present and not enabled")
    void registerUserShouldResendConfirmationToken() {
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setId(1L);
        confirmationToken.setToken(UUID.randomUUID().toString());
        confirmationToken.setCreatedAt(LocalDateTime.now());
        confirmationToken.setUser(testUser);

        when(confirmationTokenService.generateConfirmationToken(testUser))
                .thenReturn(confirmationToken);

        String token = userService.registerUser(Optional.of(testUser));
        assertThat(token).isNotEmpty().isEqualTo(confirmationToken.getToken());

        verify(confirmationTokenService).generateConfirmationToken(testUser);
    }

    @Test
    @DisplayName("registerUser(..) returns confirmation token when register is success")
    void registerUserShouldSendConfirmationToken() {
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setId(1L);
        confirmationToken.setToken(UUID.randomUUID().toString());
        confirmationToken.setCreatedAt(LocalDateTime.now());
        confirmationToken.setUser(testUser);
        String rawPassword = testUser.getPassword();

        when(confirmationTokenService.generateConfirmationToken(testUser))
                .thenReturn(confirmationToken);
        when(passwordEncoder.encode(rawPassword)).thenReturn("asdb314!@#dwaerqwer");

        String token = userService.registerUser(Optional.of(testUser));

        assertThat(testUser.getPassword()).isNotEqualTo(rawPassword);
        assertThat(token).isEqualTo(confirmationToken.getToken());

        verify(confirmationTokenService).generateConfirmationToken(testUser);
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    @DisplayName("deleteUser(..) throws not found when no user found")
    void deleteUserShouldThrowNotFound() {
        when(userRepository.findById(0L)).thenReturn(Optional.empty());

        String errorMessage = String.format("There is no user with id %d.", 0L);
        assertThatThrownBy(() -> userService.deleteUser(0L))
                .isInstanceOf(ApiNotFoundException.class)
                .hasMessage(errorMessage);

        verify(userRepository).findById(0L);
    }

    @Test
    @DisplayName("deleteUser(..) deletes user when user found")
    void deleteUserShouldDelete() {
        Long id = testUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(testUser));


        String expectedMessage = String.format("User with email %s and all his traces deleted from system!",
                testUser.getEmail());
        String actualMessage = userService.deleteUser(id);
        assertThat(expectedMessage).isEqualTo(actualMessage);

        //steps in order to delete traces, test further as they all pass
        verify(userRepository).findById(id);
        verify(waitingListRepository).deleteAllByUserId(id);
        verify(bookBorrowerRepository).deleteAllByBorrowerId(id);
        verify (bookBorrowerRepository).deleteAllByOwnerId(id);
        verify(bookOwnerRepository).deleteAllByUserId(id);
        verify(bookOwnerRepository).findAllOwnedBooks();
        verify(bookOwnerRepository).findAllOwnedBooks();
        verify(bookRepository).findAll();
        verify(confirmationTokenService).deleteAllTokensOfAnUser(id);
    }
}