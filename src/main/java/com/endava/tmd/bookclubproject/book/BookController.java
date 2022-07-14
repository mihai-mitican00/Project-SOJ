package com.endava.tmd.bookclubproject.book;

import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("books")
public class BookController {

    @Autowired
    private BookService bookService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Book>> getBooks() {
        List<Book> books = bookService.getBooks();
        if(BooleanUtilities.emptyList(books)){
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(books);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/AllAvailableBooks")
    public ResponseEntity<String> getAllAvailableBooks() {
        List<Book> availableBooks = bookService.getAllAvailableBooks();
        if (BooleanUtilities.emptyList(availableBooks)) {
            return HttpResponseUtilities.noContentFound();
        }
        StringBuilder message = new StringBuilder();
        availableBooks.forEach(book ->
                message
                .append(book.toString())
                .append("\n-----------------------------\n")
        );
        return HttpResponseUtilities.operationSuccess(message.toString());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/TitleOrAuthor")
    public ResponseEntity<String> getBooksByTitleOrAuthor(@RequestParam("title") final Optional<String> title,
                                                          @RequestParam("author") final Optional<String> author) {

        return bookService.allBooksByTitleOrAuthor(title, author);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/BookOwners")
    public ResponseEntity<List<User>> getBookOwners(@RequestParam("bookId") final Long bookId) {
        List<User> bookOwners = bookService.getBookOwnersOfBook(bookId);
        if (BooleanUtilities.emptyList(bookOwners)) {
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(bookOwners);
    }

}
