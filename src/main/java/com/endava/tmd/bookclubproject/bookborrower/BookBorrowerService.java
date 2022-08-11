package com.endava.tmd.bookclubproject.bookborrower;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    public List<BookBorrower> getAllBookBorrowers() {
        return bookBorrowerRepository.findAll();
    }

    public List<BookBorrower> getBooksThatUserGave(final Long ownerId) {
        return bookBorrowerRepository.findAllByOwnerId(ownerId);
    }

    public List<BookBorrower> getBooksThatUserRented(final Long borrowerId) {
        return bookBorrowerRepository.findAllByBorrowerId(borrowerId);
    }

    public void borrowBookFromOwner(final Long bookId, final Long borrowerId, final Long ownerId, final Long weeks) {
        checkIfRentable(bookId, borrowerId, ownerId, weeks);
        Book book = bookRepository.findById(bookId).orElse(new Book());
        User borrower = userRepository.findById(borrowerId).orElse(new User());
        BookBorrower bookBorrower = new BookBorrower(book, borrower, ownerId, weeks);
        bookBorrowerRepository.save(bookBorrower);
    }

    public void extendRentingPeriod(final Long bookId, final Long borrowerId) {
        Optional<BookBorrower> bookBorrowerOptional = bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, borrowerId);
        checkIfExtendable(bookBorrowerOptional);
        BookBorrower bookBorrower = bookBorrowerOptional.orElse(new BookBorrower());
        LocalDate returnDate = bookBorrower.getReturnDate();
        bookBorrower.setReturnDate(returnDate.plusWeeks(1));
        bookBorrowerRepository.save(bookBorrower);
    }

    public String formatBooksThatUserGave(List<BookBorrower> ownersList) {
        StringBuilder message = new StringBuilder();
        ownersList.forEach(bookBorrower ->
                message.append(bookBorrower.toStringBorrowerFocused())
                        .append("\n------------------------------\n"));
        return message.toString();
    }

    public String formatBooksThatUserRented(List<BookBorrower> borrowersList) {
        StringBuilder message = new StringBuilder();
        borrowersList.forEach(bookBorrower ->
                message.append(bookBorrower.toStringOwnerFocused())
                        .append("\n------------------------------\n"));
        return message.toString();
    }

    private void checkIfRentable(final Long bookId, final Long borrowerId, final Long ownerId, final Long weeks) {
        if (!isDataValid(bookId, borrowerId, ownerId, weeks)) {
            throw new ApiBadRequestException("Data introduced for borrow is not valid, the borrow cannot be done.");
        } else if (!isBookOwnedBy(bookId, ownerId)) {
            throw new ApiBadRequestException("The book does not belong to given owner.");
        } else if (isBookOfOwnerAlreadyBorrowed(bookId, ownerId)) {
            throw new ApiBadRequestException("The given book is already borrowed at the moment.");
        } else if (hasBorrowerAlreadyRentTheBook(bookId, borrowerId)) {
            throw new ApiBadRequestException("Given borrower has already rented this book.");
        } else if (isBookOwnedBy(bookId, borrowerId)) {
            throw new ApiBadRequestException("The user cannot rent a book that himself owns.");
        }
    }

    private void checkIfExtendable(final Optional<BookBorrower> bookBorrowerOptional){
        if (bookBorrowerOptional.isEmpty()) {
            throw new ApiBadRequestException("The borrow with given id's does not exist.");
        }
        BookBorrower bookBorrower = bookBorrowerOptional.get();
        LocalDate borrowDate = bookBorrower.getBorrowDate();
        LocalDate returnDate = bookBorrower.getReturnDate();
        LocalDate extendedReturnDate = returnDate.plusWeeks(1L);
        long weekDifference = WEEKS.between(borrowDate, extendedReturnDate);
        if (weekDifference > 5) {
            throw new ApiBadRequestException("This borrow return date cannot be extended anymore!");
        }
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
