package com.endava.tmd.bookclubproject.bookborrower;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerKey;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    public List<BookBorrower> getBooksThatUserGave(final Long userId) {
        return bookBorrowerRepository.findBooksThatUserGave(userId);
    }

    public List<BookBorrower> getBooksThatUserRented(final Long borrowerId) {
        return bookBorrowerRepository.findBooksThatUserRented(borrowerId);
    }


    public ResponseEntity<String> borrowBookFromOwner(final Long bookId, final Long borrowerId, final Long ownerId, final Long weeks) {

        if (!isDataValid(bookId, borrowerId, ownerId) || !isBookOwnedBy(bookId, ownerId)) {
            return HttpResponseUtilities.noContentFound();
        }

        if (
                isSameBookAlreadyBorrowed(bookId, ownerId)
                        || hasBorrowerAlreadyRentTheBook(bookId, borrowerId)
                        || isBookOwnedBy(bookId, borrowerId)
        ) {
            return HttpResponseUtilities.dataConflict("Borrow cannot be done.");
        }

        Book book = bookRepository.findById(bookId).orElse(new Book());
        User borrower = userRepository.findById(borrowerId).orElse(new User());
        BookBorrower bookBorrower = new BookBorrower(book, borrower, ownerId, weeks);

        bookBorrowerRepository.save(bookBorrower);
        return HttpResponseUtilities.insertDone("Book was borrowed!");
    }

    public ResponseEntity<String> extendRentingPeriod(final Long bookId, final Long ownerId) {
        Optional<BookBorrower> bookBorrowerOptional = getEntryByBookAndOwner(bookId, ownerId);
        if (bookBorrowerOptional.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }

        BookBorrower bookBorrower = bookBorrowerOptional.get();
        LocalDate borrowDate = bookBorrower.getBorrowDate();
        LocalDate returnDate = bookBorrower.getReturnDate();
        LocalDate extendedReturnDate = returnDate.plusWeeks(1L);
        long weekDifference = WEEKS.between(borrowDate, extendedReturnDate);
        if (weekDifference > 5) {
            return HttpResponseUtilities.notAcceptable("This borrow return date cannot be extended anymore!");
        }
        bookBorrower.setReturnDate(returnDate.plusWeeks(1));

        bookBorrowerRepository.save(bookBorrower);
        return HttpResponseUtilities.operationWasDone("Renting period was prolonged with one week");
    }

    public Optional<BookBorrower> getEntryByBookAndOwner(final Long bookId, final Long ownerId) {
        return bookBorrowerRepository.findEntryByBookAndOwner(bookId, ownerId);
    }

    public boolean isDataValid(final Long bookId, final Long borrowerId, final Long ownerId) {
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        Optional<User> borrowerOptional = userRepository.findById(borrowerId);
        Optional<User> ownerOptional = userRepository.findById(ownerId);
        return (bookOptional.isPresent() && borrowerOptional.isPresent() && ownerOptional.isPresent());
    }

    public boolean isBookOwnedBy(final Long bookId, final Long ownerId) {
        Optional<BookOwner> bookOwnerOptional = bookOwnerRepository.findById(new BookOwnerKey(bookId, ownerId));
        return bookOwnerOptional.isPresent();
    }

    public boolean isSameBookAlreadyBorrowed(final Long bookId, final Long ownerId) {
        Optional<BookBorrower> bookBorrowerAlreadyPresent = getEntryByBookAndOwner(bookId, ownerId);
        return bookBorrowerAlreadyPresent.isPresent();
    }

    public boolean hasBorrowerAlreadyRentTheBook(final Long bookId, final Long borrowerId) {
        BookBorrowerId bookBorrowerId = new BookBorrowerId(bookId, borrowerId);
        Optional<BookBorrower> borrowerOptional = bookBorrowerRepository.findById(bookBorrowerId);

        return borrowerOptional.isPresent();
    }


    public void deleteAllBorrowsOfAnUser(final Long userId) {
        List<BookBorrower> entries = bookBorrowerRepository.findAll();
        for (BookBorrower entry : entries) {
            if (entry.getBookBorrowerId().getBorrowerId().equals(userId) ||
                    entry.getOwnerId().equals(userId)) {
                bookBorrowerRepository.delete(entry);
            }

        }
    }

    public void deleteAllBorrowsOfAnBook(final Long bookId) {
        List<BookBorrower> entries = bookBorrowerRepository.findAll();
        for (BookBorrower entry : entries) {
            if (entry.getBook().getId().equals(bookId)) {
                bookBorrowerRepository.delete(entry);
            }

        }
    }
}
