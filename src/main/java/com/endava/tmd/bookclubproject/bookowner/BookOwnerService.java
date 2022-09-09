package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.exception.ApiNotFoundException;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.endava.tmd.bookclubproject.utilities.BooleanUtilities.anyEmptyStringElements;
import static com.endava.tmd.bookclubproject.utilities.BooleanUtilities.anyNullElements;


@Service
public class BookOwnerService {

    @Autowired
    private BookOwnerRepository bookOwnerRepository;

    @Autowired
    private BookBorrowerRepository bookBorrowerRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    public BookOwnerService() {
    }

    public List<BookOwner> getAllBookOwners() {
        return bookOwnerRepository.findAll();
    }

    public void addBookAsUser(final User user, final Book book) {

        if (book == null || hasIncompleteData(book)) {
            throw new ApiBadRequestException("Book has incomplete data, enter something on all fields!");
        }

        BookOwner bookOwner = new BookOwner(book, user);

        //Optional to check if book is already present in book table
        Optional<Book> bookAlreadyPresent =
                bookRepository.findByTitleAndAuthorAndEdition(
                        book.getTitle(),
                        book.getAuthor(),
                        book.getEdition()
                );


        //Check if book already exists in the virtual shelter
        if (bookAlreadyPresent.isEmpty()) {
            bookRepository.save(book);
            bookOwnerRepository.save(bookOwner);
        } else {
            List<Book> booksOfUser = bookOwnerRepository.findBooksOfUser(user.getId());

            //Check if book was already added by given user
            if (booksOfUser.contains(bookAlreadyPresent.get())) {
                throw new ApiBadRequestException("You already added this book.");
            } else {
                bookOwner.setBook(bookAlreadyPresent.get());
                bookOwnerRepository.save(bookOwner);
            }

        }

    }

    public void deleteBookAsUser(final Long bookId, final Long ownerId) {
        //throw badRequest if book or owner not found or the owner does not have this book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ApiNotFoundException(String.format("There is no book with id %d", bookId)));

        BookOwner bookOwner = bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId)
                .orElseThrow(() -> new ApiBadRequestException("Given book does not belong to you."));


        //return it from borrow immediately
        bookBorrowerRepository.deleteByBookIdAndOwnerId(bookId, ownerId);
        bookOwnerRepository.delete(bookOwner);

        //check if it was the last book owned of that type
        List<BookOwner> leftEntries = bookOwnerRepository.findAllByBookId(bookId);
        if (leftEntries.isEmpty()) {
            bookRepository.delete(book);
        }
    }


    private boolean hasIncompleteData(final Book book) {
        String[] bookData = {book.getTitle(), book.getAuthor(), book.getEdition()};
        return anyNullElements(bookData) || anyEmptyStringElements(bookData);
    }

}
