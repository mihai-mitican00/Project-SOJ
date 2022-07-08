package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerService;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerService;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import com.endava.tmd.bookclubproject.waitinglist.WaitingList;
import com.endava.tmd.bookclubproject.waitinglist.WaitingListRepository;
import com.endava.tmd.bookclubproject.waitinglist.WaitingListService;
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
    private BookOwnerRepository bookOwnerRepository;

    @Autowired
    private BookOwnerService bookOwnerService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookBorrowerRepository bookBorrowerRepository;

    @Autowired
    private BookBorrowerService bookBorrowerService;

    @Autowired
    private WaitingListRepository waitingListRepository;

    @Autowired
    private WaitingListService waitingListService;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(final Long userId) {
        return userRepository.findById(userId);
    }


    public Optional<User> getUserByUsernameOrByEmail(final Optional<String> username, final Optional<String> email) {
        return userRepository.findUserByUsernameOrEmail(username, email);
    }


    public ResponseEntity<String> registerUser(Optional<User> userOptional) {
        if (userOptional.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }

        User user = userOptional.get();

        Optional<User> userByUsernameOrEmail = getUserByUsernameOrByEmail(
                Optional.of(user.getUsername()),
                Optional.of(user.getEmail())
        );

        if (userByUsernameOrEmail.isPresent()) {
            return HttpResponseUtilities.dataConflict("User already exists!");
        }

        //encode password
        int strength = 10;
        BCryptPasswordEncoder bCryptPasswordEncoder =
                new BCryptPasswordEncoder(strength, new SecureRandom());
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);
        return HttpResponseUtilities.insertDone("User account created with success!");
    }


    public ResponseEntity<String> deleteUser(Optional<Long> userId) {
        if (userId.isEmpty()) {
            return HttpResponseUtilities.wrongParameters();
        }

        Optional<User> optionalUser = userRepository.findById(userId.get());
        if (optionalUser.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }

        waitingListService.deleteAllEntriesOfAnUser(userId.get());
        bookBorrowerService.deleteAllBorrowsOfAnUser(userId.get());
        bookOwnerService.deleteAllBooksOwnedByAnUser(userId.get());
        userRepository.delete(optionalUser.get());
        return HttpResponseUtilities.operationWasDone("User " + optionalUser.get().getUsername() + " and all his work deleted!");
    }

    public List<Book> getBooksOwned(final Long userId) {
        List<BookOwner> bookOwnerEntries = bookOwnerRepository.findAll();

        User user = userRepository.findById(userId).orElse(null);

        return bookOwnerEntries.stream()
                .filter(bo -> bo.getUser().equals(user))
                .map(BookOwner::getBook)
                .toList();
    }


}
