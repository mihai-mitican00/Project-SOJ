package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public List<BookOwner> getAllBookOwners() {
        return bookOwnerRepository.findAll();
    }

    public void addBookByUserId(final Long userId, final Optional<Book> bookOptional) {
        //get badRequest if user not found or book has incomplete data
        checkValidDataForAdd(userId, bookOptional);

        Optional<User> userOptional = userRepository.findById(userId);
        User user = userOptional.orElse(new User());
        Book book = bookOptional.orElse(new Book());
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
            List<Book> booksOfUser = bookOwnerRepository.findBooksOfUser(userId);

            //Check if book was already added by given user
            if (booksOfUser.contains(bookAlreadyPresent.get())) {
                throw new ApiBadRequestException("User with id " + user.getId() + " already added this book!");
            } else {
                bookOwner.setBook(bookAlreadyPresent.get());
                bookOwnerRepository.save(bookOwner);
            }

        }

    }

    public void deleteBookFromUser(final Long bookId, final Long ownerId) {
        //get badRequest if book or owner not found or the owner does not have this book
        checkValidDataForDelete(bookId, ownerId);

        Book book = bookRepository.findById(bookId).orElse(new Book());
        BookOwner bookOwner = bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId).orElse(new BookOwner());

        //return it from borrow immediately
        bookBorrowerRepository.deleteByBookIdAndOwnerId(bookId, ownerId);
        bookOwnerRepository.delete(bookOwner);

        //check if it was the last book owned of that type
        List<BookOwner> leftEntries = bookOwnerRepository.findAllByBookId(bookId);
        if (leftEntries.isEmpty()) {
            bookRepository.delete(book);
        }
    }

    private void checkValidDataForAdd(final Long userId, final Optional<Book> bookOptional) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ApiBadRequestException("User with given id does not exist.");
        }
        if (bookOptional.isEmpty() || hasIncompleteData(bookOptional)) {
            throw new ApiBadRequestException("Book has incomplete data, enter something on all fields!");
        }
    }

    private void checkValidDataForDelete(final Long bookId, final Long ownerId) {
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        Optional<User> ownerOptional = userRepository.findById(ownerId);
        if (bookOptional.isEmpty() || ownerOptional.isEmpty()) {
            throw new ApiBadRequestException("There is no book or owner with given id's.");
        }

        Optional<BookOwner> bookOwnerOptional = bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId);
        if (bookOwnerOptional.isEmpty()) {
            throw new ApiBadRequestException("Given book does not belong to given owner.");
        }
    }

    private boolean hasIncompleteData(Optional<Book> bookOptional) {
        Book book = bookOptional.orElse(new Book());
        String[] bookData = {book.getTitle(), book.getAuthor(), book.getEdition()};
        return anyNullElements(bookData) || anyEmptyStringElements(bookData);
    }

}
