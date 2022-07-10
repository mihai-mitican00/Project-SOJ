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
import com.endava.tmd.bookclubproject.waitinglist.WaitingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<User> getUsers() {
        return userRepository.findAll();
    }


    public Optional<User> getUserByUsernameOrByEmail(final Optional<String> username, final Optional<String> email) {
        return userRepository.findUserByUsernameOrEmail(username, email);
    }


    public ResponseEntity<String> registerUser(Optional<User> userOptional) {
        if (userOptional.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }

        if(hasIncompleteData(userOptional)){
            return HttpResponseUtilities.notAcceptable("Details for user are not complete!");
        }

        User user = userOptional.get();

        Optional<User> userByUsernameOrEmail = getUserByUsernameOrByEmail(
                Optional.of(user.getUsername()),
                Optional.of(user.getEmail())
        );

        if (userByUsernameOrEmail.isPresent()) {
            return HttpResponseUtilities.dataConflict("User with username or email already exists!");
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

        deleteWaitingListEntriesOfAnUser(userId.get());
        deleteAllBorrowsOfAnUser(userId.get());
        deleteAllBooksOwnedByAnUser(userId.get());
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


    private boolean hasIncompleteData(Optional<User> userOptional){

        User user = userOptional.orElse(new User());
        String [] userData = {user.getEmail(), user.getFirstName(), user.getLastName(), user.getPassword(), user.getUsername()};
        return BooleanUtilities.anyNullParameters(userData) || BooleanUtilities.anyEmptyString(userData);
    }

    private void deleteWaitingListEntriesOfAnUser(final Long userId){
        List<WaitingList> entries = waitingListRepository.findAll();
        for(WaitingList entry : entries){
            if(entry.getUserId().equals(userId)){
                waitingListRepository.delete(entry);
            }
        }
    }

    private void deleteAllBorrowsOfAnUser(final Long userId) {
        List<BookBorrower> entries = bookBorrowerRepository.findAll();
        for (BookBorrower entry : entries) {
            if (entry.getBookBorrowerId().getBorrowerId().equals(userId) ||
                    entry.getOwnerId().equals(userId)) {
                bookBorrowerRepository.delete(entry);
            }

        }
    }

    private void deleteAllBooksOwnedByAnUser(final Long userId) {
        List<BookOwner> bookOwners = bookOwnerRepository.findAll();

        List<Book> booksInOwnerTable = bookOwners.stream().map(BookOwner::getBook).collect(Collectors.toList());
        List<Book> booksInBooksTable = bookRepository.findAll();

        for(BookOwner entry : bookOwners){
            Book book = entry.getBook();
            if(entry.getUser().getId().equals(userId)){
                bookOwnerRepository.delete(entry);
                booksInOwnerTable.remove(book);
                if(booksInBooksTable.contains(book) && !booksInOwnerTable.contains(book)){
                    bookRepository.delete(book);
                    booksInBooksTable.remove(book);
                }
            }
        }
    }
}
