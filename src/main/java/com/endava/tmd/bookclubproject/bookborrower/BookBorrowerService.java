package com.endava.tmd.bookclubproject.bookborrower;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities.*;
import static java.time.temporal.ChronoUnit.WEEKS;

@Service
public class BookBorrowerService {

    @Autowired
    private BookBorrowerRepository bookBorrowerRepository;

    @Autowired
    private BookOwnerRepository bookOwnerRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<List<BookBorrower>> getAllBookBorrowers() {
        List<BookBorrower> listOfEntries = bookBorrowerRepository.findAll();
        if (BooleanUtilities.emptyList(listOfEntries)) {
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(listOfEntries);

    }

    public ResponseEntity<String> getBooksThatUserGave(final Long ownerId) {
        List<BookBorrower> borrowerList = bookBorrowerRepository.findAllByOwnerId(ownerId);
        if (borrowerList.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }

        StringBuilder message = new StringBuilder();
        borrowerList.forEach(bookBorrower ->
                message.append(bookBorrower.toStringBorrowerFocused())
                        .append("\n------------------------------\n"));

        return HttpResponseUtilities.operationSuccess(message.toString());
    }

    public ResponseEntity<String> getBooksThatUserRented(final Long borrowerId) {
        List<BookBorrower> borrowerList = bookBorrowerRepository.findAllByBorrowerId(borrowerId);
        if (BooleanUtilities.emptyList(borrowerList)) {
            return HttpResponseUtilities.noContentFound();
        }

        StringBuilder message = new StringBuilder();
        borrowerList.forEach(bookBorrower ->
                message.append(bookBorrower.toStringOwnerFocused())
                        .append("\n------------------------------\n"));

        return HttpResponseUtilities.operationSuccess(message.toString());
    }

    public ResponseEntity<String> borrowBookFromOwner(final Long bookId, final Long borrowerId, final Long ownerId, final Long weeks) {

        if (!isDataValid(bookId, borrowerId, ownerId, weeks)) {
            return badRequest("Data introduced for borrow is not valid, the borrow cannot be done.");
        } else if (!isBookOwnedBy(bookId, ownerId)) {
            return badRequest("The book does not belong to given owner.");
        } else if (isBookOfOwnerAlreadyBorrowed(bookId, ownerId)) {
            return badRequest("The given book is already borrowed at the moment.");
        } else if (hasBorrowerAlreadyRentTheBook(bookId, borrowerId)) {
            return badRequest("Given borrower has already rented this book.");
        } else if (isBookOwnedBy(bookId, borrowerId)) {
            return badRequest("The user cannot rent a book that himself owns.");
        }

        Book book = bookRepository.findById(bookId).orElse(new Book());
        User borrower = userRepository.findById(borrowerId).orElse(new User());
        BookBorrower bookBorrower = new BookBorrower(book, borrower, ownerId, weeks);

        bookBorrowerRepository.save(bookBorrower);
        return insertSuccess
                ("Book with id " + book.getId()
                        + " was borrowed by user with id " + borrower.getId()
                        + " for " + weeks + " weeks");
    }

    public ResponseEntity<String> extendRentingPeriod(final Long bookId, final Long borrowerId) {
        Optional<BookBorrower> bookBorrowerOptional = bookBorrowerRepository
                .findByBookIdAndBorrowerId(bookId, borrowerId);

        if (bookBorrowerOptional.isEmpty()) {
            return badRequest("The borrow with given id's does not exist.");
        }

        BookBorrower bookBorrower = bookBorrowerOptional.get();
        LocalDate borrowDate = bookBorrower.getBorrowDate();
        LocalDate returnDate = bookBorrower.getReturnDate();
        LocalDate extendedReturnDate = returnDate.plusWeeks(1L);
        long weekDifference = WEEKS.between(borrowDate, extendedReturnDate);
        if (weekDifference > 5) {
            return badRequest("This borrow return date cannot be extended anymore!");
        }
        bookBorrower.setReturnDate(returnDate.plusWeeks(1));

        bookBorrowerRepository.save(bookBorrower);
        return operationSuccess("Renting period was prolonged with one week");
    }

    private boolean isDataValid(final Long bookId, final Long borrowerId, final Long ownerId, final Long weeks) {
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        Optional<User> borrowerOptional = userRepository.findById(borrowerId);
        Optional<User> ownerOptional = userRepository.findById(ownerId);
        return (bookOptional.isPresent() && borrowerOptional.isPresent() && ownerOptional.isPresent() && (weeks >= 1 && weeks <= 4));
    }

    private boolean isBookOwnedBy(final Long bookId, final Long ownerId) {
        Optional<BookOwner> bookOwnerOptional = bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId);
        return bookOwnerOptional.isPresent();
    }

    private boolean isBookOfOwnerAlreadyBorrowed(final Long bookId, final Long ownerId) {
        Optional<BookBorrower> bookByOwner = bookBorrowerRepository.findByBookIdAndOwnerId(bookId, ownerId);
        return bookByOwner.isPresent();
    }

    private boolean hasBorrowerAlreadyRentTheBook(final Long bookId, final Long borrowerId) {
        Optional<BookBorrower> borrowerOptional = bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, borrowerId);
        return borrowerOptional.isPresent();
    }
}
