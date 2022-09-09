package com.endava.tmd.bookclubproject.waitinglist;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.*;

@Service
public class WaitingListService {

    @Autowired
    private WaitingListRepository waitingListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookOwnerRepository bookOwnerRepository;

    @Autowired
    private BookBorrowerRepository bookBorrowerRepository;

    public List<WaitingList> getAllOnWaitingList() {
        return waitingListRepository.findAll();
    }

    public void addUserOnList(final Long bookId, final Long ownerId, final Long userId) {
        checkValidDataForAdd(bookId, ownerId, userId);
        WaitingList entry = new WaitingList(bookId, ownerId, userId);
        waitingListRepository.save(entry);
    }

    private void checkValidDataForAdd(final Long bookId, final Long ownerId, final Long userId) {
        if (invalidObjects(bookId, ownerId, userId)) {
            throw new ApiBadRequestException("Invalid data, some objects do not exist.");
        }

        if (isBookNotBorrowed(bookId, ownerId)) {
            throw new ApiBadRequestException("You cannot add yourself on the waiting list for a book that is not already rented!");
        }

        if (isUserOwningTheBook(bookId, userId)) {
            throw new ApiBadRequestException("User cannot be added on waiting list for his own book.");
        }

        if (bookAlreadyBorrowedByThisUser(bookId, userId)) {
            String errorMessage = String.format("User having id %d is already renting book with id %d", userId, bookId);
            throw new ApiBadRequestException(errorMessage);
        }

        if (entryAlreadyPresent(bookId, userId)) {
            String errorMessage = String.format(
                    "User with id %d already added himself on waiting list for book with id %d",
                    userId,
                    bookId);
            throw new ApiBadRequestException(errorMessage);
        }

    }


    private boolean invalidObjects(final Long bookId, final Long ownerId, final Long userId) {
        Optional<BookOwner> bookOwnerOptional = bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId);
        Optional<User> userOptional = userRepository.findById(userId);

        return bookOwnerOptional.isEmpty() || userOptional.isEmpty();
    }

    private boolean entryAlreadyPresent(final Long bookId, final Long userId) {
        Optional<WaitingList> optionalWaitingList = waitingListRepository.findByBookIdAndUserId(bookId, userId);
        return optionalWaitingList.isPresent();
    }

    private boolean isBookNotBorrowed(final Long bookId, final Long ownerId) {
        Optional<BookBorrower> bookBorrowerOptional = bookBorrowerRepository.findByBookIdAndOwnerId(bookId, ownerId);
        return bookBorrowerOptional.isEmpty();
    }

    private boolean bookAlreadyBorrowedByThisUser(final Long bookId, final Long userId) {
        Optional<BookBorrower> borrowDoneByUser = bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, userId);
        return borrowDoneByUser.isPresent();
    }

    private boolean isUserOwningTheBook(final Long bookId, final Long userId) {
        Optional<BookOwner> bookOwnerOptional = bookOwnerRepository.findByBookIdAndUserId(bookId, userId);
        return bookOwnerOptional.isPresent();
    }
}
