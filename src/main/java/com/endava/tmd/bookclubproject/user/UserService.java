package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerService;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerService;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import com.endava.tmd.bookclubproject.waitinglist.WaitingList;
import com.endava.tmd.bookclubproject.waitinglist.WaitingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

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

    public ResponseEntity<List<User>> getAllUsers() {
        List<User> usersList = userRepository.findAll();
        if (BooleanUtilities.emptyList(usersList)) {
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(usersList);
    }


    public ResponseEntity<String> registerUser(Optional<User> userOptional) {

        if (userOptional.isEmpty() || hasIncompleteData(userOptional)) {
            return HttpResponseUtilities.notAcceptable("Details for user are not complete!");
        }

        User user = userOptional.get();

        Optional<User> userByUsernameOrEmail = userRepository.findUserByUsernameOrEmail(
                Optional.of(user.getUsername()),
                Optional.of(user.getEmail())
        );

        if (userByUsernameOrEmail.isPresent()) {
            return HttpResponseUtilities.badRequest("User with username or email already exists!");
        }

        //encode password
        int strength = 10;
        BCryptPasswordEncoder bCryptPasswordEncoder =
                new BCryptPasswordEncoder(strength, new SecureRandom());
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);
        return HttpResponseUtilities.insertSuccess("User account created with success!");
    }

    public ResponseEntity<String> deleteUser(final Long userId) {

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }

        waitingListRepository.deleteAllByUserId(userId);
        bookBorrowerRepository.deleteAllByBorrowerId(userId);
        bookBorrowerRepository.deleteAllByOwnerId(userId);
        deleteAllBooksOwnedByAnUser(userId);
        userRepository.delete(optionalUser.get());
        return HttpResponseUtilities.operationSuccess("User " + optionalUser.get().getUsername() + " and all his work deleted!");
    }

    public ResponseEntity<List<Book>> getBooksOfUser(final Long userId) {
        List<Book> booksOwned = bookOwnerRepository.findBooksOfUser(userId);
        if (booksOwned.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(booksOwned);
    }

    private boolean hasIncompleteData(Optional<User> userOptional) {
        User user = userOptional.orElse(new User());
        String[] userData = {user.getEmail(), user.getFirstName(), user.getLastName(), user.getPassword(), user.getUsername()};
        return BooleanUtilities.anyNullParameters(userData) || BooleanUtilities.anyEmptyString(userData);
    }

    private void deleteAllBooksOwnedByAnUser(final Long userId) {
        List<BookOwner> bookOwners = bookOwnerRepository.findAll();

        List<Book> booksInOwnerTable = bookOwnerRepository.findAllOwnedBooks();
        List<Book> booksInBooksTable = bookRepository.findAll();

        for (BookOwner entry : bookOwners) {
            Book book = entry.getBook();
            if (entry.getUser().getId().equals(userId)) {
                bookOwnerRepository.delete(entry);
                booksInOwnerTable.remove(book);
                if (booksInBooksTable.contains(book) && !booksInOwnerTable.contains(book)) {
                    bookRepository.delete(book);
                    booksInBooksTable.remove(book);
                }
            }
        }
    }


}
