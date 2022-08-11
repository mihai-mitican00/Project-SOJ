package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.registration.token.ConfirmationToken;
import com.endava.tmd.bookclubproject.registration.token.ConfirmationTokenRepository;
import com.endava.tmd.bookclubproject.registration.token.ConfirmationTokenService;
import com.endava.tmd.bookclubproject.waitinglist.WaitingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.endava.tmd.bookclubproject.utilities.BooleanUtilities.*;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookOwnerRepository bookOwnerRepository;

    @Autowired
    private BookBorrowerRepository bookBorrowerRepository;

    @Autowired
    private WaitingListRepository waitingListRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with email %s not found", email)));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Book> getBooksOfUser(final Long userId) {
        return bookOwnerRepository.findBooksOfUser(userId);
    }


    public String registerUser(Optional<User> userOptional) {
        //check if user data is complete and if user with same email or username not already registered
        checkValidDataForRegister(userOptional);

        User user = userOptional.orElse(new User());
        encryptUserPassword(user);
        userRepository.save(user);

        //Now send confirmation token
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                user
        );
        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return token;
    }

    public void enableUserAccount(final String email){
        User user = userRepository
                .findUserByEmail(email)
                .orElseThrow(() -> new ApiBadRequestException("User not found!"));

        user.setEnabled(true);
    }

    public void deleteUser(final Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ApiBadRequestException("User with given id does not exist.");
        }

        deleteUserTrace(userId);
        userRepository.delete(optionalUser.get());
    }


    private void checkValidDataForRegister(Optional<User> userOptional) {
        if (userOptional.isEmpty() || hasIncompleteData(userOptional)) {
            throw new ApiBadRequestException("Details for user are not complete!");
        }
        User user = userOptional.get();

        Optional<User> userByUsernameOrEmail = userRepository.findUserByUsernameOrEmail(
                Optional.of(user.getUsername()),
                Optional.of(user.getEmail())
        );

        if (userByUsernameOrEmail.isPresent()) {
            throw new ApiBadRequestException("User with username or email already exists!");
        }
    }

    private void encryptUserPassword(final User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    }

    private boolean hasIncompleteData(Optional<User> userOptional) {
        User user = userOptional.orElse(new User());
        String[] userData = {user.getEmail(), user.getFirstName(), user.getLastName(), user.getPassword(), user.getUsername()};
        return anyNullElements(userData) || anyEmptyStringElements(userData);
    }

    private void deleteUserTrace(final Long userId) {
        //delete trace
        waitingListRepository.deleteAllByUserId(userId);
        List<Book> booksOwnedByUser = getBooksOfUser(userId);
        booksOwnedByUser.forEach(book -> waitingListRepository.deleteAllByBookId(book.getId()));


        //delete borrows made by user or borrows where he gave books
        bookBorrowerRepository.deleteAllByBorrowerId(userId);
        bookBorrowerRepository.deleteAllByOwnerId(userId);

        //delete all books owned by an user and the book if he has the only copy
        bookOwnerRepository.deleteAllByUserId(userId);

        List<Book> booksOwned = bookOwnerRepository.findAllOwnedBooks();
        List<Book> books = bookRepository.findAll();
        books.stream().filter(book -> !booksOwned.contains(book)).forEach(book -> bookRepository.delete(book));
    }


}
