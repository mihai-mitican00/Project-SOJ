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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static com.endava.tmd.bookclubproject.utilities.BooleanUtilities.anyEmptyStringElements;
import static com.endava.tmd.bookclubproject.utilities.BooleanUtilities.anyNullElements;

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

    public Optional<User> getUserById(final Long id) {
        return userRepository.findById(id);
    }

    public List<Book> getBooksOfUser(final Long userId) {
        return bookOwnerRepository.findBooksOfUser(userId);
    }

    public String registerUser(Optional<User> userOptional) {
        //check if user data is complete and if user with same email or username not already registered
        if (userOptional.isEmpty() || hasIncompleteData(userOptional.get())) {
            throw new ApiBadRequestException("Details for user are not complete!");
        }

        User user = userOptional.get();
        String email = user.getEmail();
        Optional<User> userByEmail = userRepository.findUserByEmail(email);

        ConfirmationToken confirmationToken;
        //User account exists and has already confirmed activation link
        if (userByEmail.isPresent() && userByEmail.get().isEnabled()) {
            String errorMessage = String.format("User with email %s is already registered!", email);
            throw new ApiBadRequestException(errorMessage);
        }
        //Account exists but confirmation link not activated
        else if (userByEmail.isPresent() && !userByEmail.get().isEnabled()) {
            confirmationToken = confirmationTokenService.generateConfirmationToken(userByEmail.get());
        }
        //Account does not exist.
        else {
            encryptUserPassword(user);
            userRepository.save(user);
            confirmationToken = confirmationTokenService.generateConfirmationToken(user);
        }

        //save token in database
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        //Now send further away confirmation token
        return confirmationToken.getToken();
    }

    public void enableUserAccount(final String email) {
        User user = userRepository
                .findUserByEmail(email)
                .orElseThrow(() -> new ApiNotFoundException("User not found!"));

        user.setEnabled(true);
    }


    public String deleteUser(final Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ApiNotFoundException(String.format("There is no user with id %d.", userId));
        }

        deleteUserTrace(userId);
        userRepository.deleteById(userId);
        return String.format("User with email %s and all his traces deleted from system!", userOptional.get().getEmail());
    }

    private void deleteUserTrace(final Long userId) {
        //delete trace
        waitingListRepository.deleteAllByUserId(userId);
        List<Book> booksOwnedByUser = getBooksOfUser(userId);
        booksOwnedByUser.forEach(book -> waitingListRepository.deleteAllByBookId(book.getId()));


        //delete borrows made by user or borrows where he gave books
        bookBorrowerRepository.deleteAllByBorrowerId(userId);
        bookBorrowerRepository.deleteAllByOwnerId(userId);

        //delete all books owned by a user and the book if he has the only copy
        bookOwnerRepository.deleteAllByUserId(userId);

        List<Book> booksOwned = bookOwnerRepository.findAllOwnedBooks();
        List<Book> books = bookRepository.findAll();
        books.stream().filter(book -> !booksOwned.contains(book)).forEach(book -> bookRepository.delete(book));

        //deleting all his previous tokens
        confirmationTokenService.deleteAllTokensOfAnUser(userId);
    }

    private void encryptUserPassword(final User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    }

    private boolean hasIncompleteData(final User user) {
        String[] userData = {user.getEmail(), user.getFirstName(), user.getLastName(), user.getPassword(), user.getUsername()};
        return anyNullElements(userData) || anyEmptyStringElements(userData);
    }


}
