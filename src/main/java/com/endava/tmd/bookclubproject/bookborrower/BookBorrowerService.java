package com.endava.tmd.bookclubproject.bookborrower;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.exception.ApiNotFoundException;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<BookBorrower> getBorrowsWhereUserGave(final Long ownerId) {
        return bookBorrowerRepository.findAllByOwnerId(ownerId);
    }

    public List<BookBorrower> getBorrowsWhereUserReceived(final Long borrowerId) {
        return bookBorrowerRepository.findAllByBorrowerId(borrowerId);
    }

    public String borrowBookFromOwner(final Long bookId, final Long borrowerId, final Long ownerId, final Long weeks) {
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        Optional<User> borrowerOptional = userRepository.findById(borrowerId);
        Optional<User> ownerOptional = userRepository.findById(ownerId);

        if (bookOptional.isEmpty() ||
                borrowerOptional.isEmpty() ||
                ownerOptional.isEmpty() ||
                !validRentingPeriod(weeks)) {
            throw new ApiNotFoundException("Invalid params, can not proceed with the borrow!");
        }

        Book book = bookOptional.get();
        User borrower = borrowerOptional.get();

        checkIfRentable(bookId, borrowerId, ownerId);
        BookBorrower bookBorrower = new BookBorrower(book, borrower, ownerId, weeks);
        bookBorrowerRepository.save(bookBorrower);
        return String.format("Hello %s!%nYou just borrowed \"%s\".%nHappy reading!",
                borrower.getUsername(),
                book.getTitle());
    }

    public String extendRentingPeriod(final Long bookId, final Long borrowerId) {
        BookBorrower bookBorrower = bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, borrowerId)
                .orElseThrow(() -> new ApiNotFoundException("The borrow with given id's does not exist."));

        checkIfExtendableReturnDate(bookBorrower);

        LocalDate returnDate = bookBorrower.getReturnDate();
        bookBorrower.setReturnDate(returnDate.plusWeeks(1));
        bookBorrowerRepository.save(bookBorrower);
        return String.format("You just extended renting period for book \"%s\" for another week",
                bookBorrower.getBook().getTitle());
    }

    public String formatBooksThatUserGave(List<BookBorrower> ownersList) {
        StringBuilder message = new StringBuilder();
        ownersList.forEach(bookBorrower ->
                message.append(bookBorrower.toStringBorrowerFocused())
                        .append("\n------------------------------\n"));
        return message.toString();
    }

    public String formatBooksThatUserReceived(List<BookBorrower> borrowersList) {
        StringBuilder message = new StringBuilder();
        borrowersList.forEach(bookBorrower ->
                message.append(bookBorrower.toStringOwnerFocused())
                        .append("\n------------------------------\n"));
        return message.toString();
    }

    private void checkIfRentable(final Long bookId, final Long borrowerId, final Long ownerId) {
        if (isBookOwnedBy(bookId, borrowerId)) {
            throw new ApiBadRequestException("The user cannot rent a book that himself owns.");
        } else if (!isBookOwnedBy(bookId, ownerId)) {
            throw new ApiBadRequestException("The book does not belong to given owner.");
        } else if (isBookOfOwnerAlreadyBorrowed(bookId, ownerId)) {
            throw new ApiBadRequestException("The given book is already borrowed at the moment.");
        }
    }

    private void checkIfExtendableReturnDate(final BookBorrower entry) {
        LocalDate borrowDate = entry.getBorrowDate();
        LocalDate returnDate = entry.getReturnDate();
        LocalDate extendedReturnDate = returnDate.plusWeeks(1L);
        long weekDifference = WEEKS.between(borrowDate, extendedReturnDate);
        if (weekDifference > 5) {
            throw new ApiBadRequestException("This borrow return date cannot be extended anymore!");
        }
    }

    private boolean validRentingPeriod(final Long weeks) {
        return weeks >= 1 && weeks <= 4;
    }

    private boolean isBookOwnedBy(final Long bookId, final Long ownerId) {
        Optional<BookOwner> bookOwnerOptional = bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId);
        return bookOwnerOptional.isPresent();
    }

    private boolean isBookOfOwnerAlreadyBorrowed(final Long bookId, final Long ownerId) {
        Optional<BookBorrower> bookByOwner = bookBorrowerRepository.findByBookIdAndOwnerId(bookId, ownerId);
        return bookByOwner.isPresent();
    }

//    private boolean hasBorrowerAlreadyRentTheBook(final Long bookId, final Long borrowerId) {
//        Optional<BookBorrower> borrowerOptional = bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, borrowerId);
//        return borrowerOptional.isPresent();
//    }


}
