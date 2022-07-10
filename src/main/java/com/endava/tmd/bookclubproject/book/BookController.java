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
    public List<Book> getBooks() {
        return bookService.getBooks();
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
        return HttpResponseUtilities.operationWasDone(message.toString());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/TitleOrAuthor")
    public ResponseEntity<String> getBooksByTitleOrAuthor(@RequestParam("title") final Optional<String> title,
                                                          @RequestParam("author") final Optional<String> author) {

        return bookService.allBooksByTitleOrAuthor(title, author);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/BookOwners")
    public Object getBookOwners(@RequestParam("bookId") final Optional<Long> bookId) {
        if (BooleanUtilities.anyEmptyParameters(bookId)) {
            return HttpResponseUtilities.wrongParameters();
        }
        List<User> bookOwners = bookService.getBookOwners(bookId.orElse(null));
        if (BooleanUtilities.emptyList(bookOwners)) {
            return HttpResponseUtilities.noContentFound();
        }
        return bookOwners;
    }

}
