package com.endava.tmd.bookclubproject.book;

import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("books")
public class BookController {

    @Autowired
    private BookService bookService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Book> getBooks() {
        return bookService.getBooks();
    }

    @RequestMapping(method = RequestMethod.GET, params = "bookId")
    public Object getBookById(@RequestParam final Long bookId) {
        Optional<Book> optionalBook = bookService.getBookById(bookId);

        if (BooleanUtilities.anyEmptyParameters(optionalBook)) {
            return HttpResponseUtilities.noContentFound();
        }
        return optionalBook;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/AllAvailableBooks")
    public Object getAllAvailableBooks() {
        List<Book> availableBooks = bookService.getAllAvailableBooks();

        if (BooleanUtilities.emptyList(availableBooks)) {
            return HttpResponseUtilities.noContentFound();
        }
        return availableBooks;
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


    @RequestMapping(method = RequestMethod.DELETE, params = "bookId")
    public void deleteBook(@RequestParam final Long bookId) {
        bookService.deleteBook(bookId);
    }


}
