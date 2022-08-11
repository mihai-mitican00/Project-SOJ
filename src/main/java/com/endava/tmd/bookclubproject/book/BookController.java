package com.endava.tmd.bookclubproject.book;

import com.endava.tmd.bookclubproject.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static org.hibernate.cfg.AvailableSettings.USER;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Book")
@RestController
@RequestMapping("books")
public class BookController {

    @Autowired
    private BookService bookService;

    @PreAuthorize("hasAuthority('book:read')")
    @RequestMapping(method = RequestMethod.GET, value = "/AllBooks")
    @Operation(
            summary = "Get all books.",
            description = "Getting all distinct books present in the virtual shelter.",
            responses = {
                    @ApiResponse(
                            description = "See all the books with success.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
                    ),
                    @ApiResponse(
                            description = "No Books are shared in the virtual shelter.",
                            responseCode = "204",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            return noContent().build();
        }
        return ok(books);
    }

    @PreAuthorize("hasAuthority('book:read')")
    @RequestMapping(method = RequestMethod.GET, value = "/AllAvailableBooks")
    @Operation(
            summary = "Get all available for renting books.",
            description = "Getting all books that are available for renting.",
            responses = {
                    @ApiResponse(
                            description = "See all the books that are available for renting with success.",
                            responseCode = "200",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "No Books are available for renting.",
                            responseCode = "204",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<String> getAllAvailableBooks() {
        List<Book> availableBooks = bookService.getAllAvailableBooks();
        if (availableBooks.isEmpty()) {
            return noContent().build();
        }
        String message = bookService.formatAvailableBooks(availableBooks);
        return ok(message);
    }

    @PreAuthorize("hasAuthority('book:read')")
    @RequestMapping(method = RequestMethod.GET, value = "/TitleOrAuthor")
    @Operation(
            summary = "Get books by title or author.",
            description = "Get all books with given title or given author, and show if they are available or not.",
            responses = {
                    @ApiResponse(
                            description = "See the books found by title or author.",
                            responseCode = "200",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "No Books found by given title or author.",
                            responseCode = "204",
                            content = @Content
                    )
            }
    )

    public ResponseEntity<String> getBooksByTitleOrAuthor(@RequestParam("title") final Optional<String> title,
                                                          @RequestParam("author") final Optional<String> author)
    {
        List<Book> booksByTitleOrAuthor = bookService.allBooksByTitleOrAuthor(title, author);
        if (booksByTitleOrAuthor.isEmpty()) {
            return noContent().build();
        }
        String message = bookService.formatBooksByTitleOrAuthor(booksByTitleOrAuthor);
        return ok(message);
    }
    @PreAuthorize("hasAuthority('user:read')")
    @RequestMapping(method = RequestMethod.GET, value = "/BookOwners")
    @Operation(
            summary = "Get book owners of a book.",
            description = "Get all owners of a given by id book, all books have at least one owner.",
            responses = {
                    @ApiResponse(
                            description = "See the owners of chosen book.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))
                    ),
                    @ApiResponse(
                            description = "There is no book for given id.",
                            responseCode = "204",
                            content = @Content
                    ),
            }
    )
    public ResponseEntity<List<User>> getBookOwnersOfBook(@RequestParam("bookId") final Long bookId) {
        List<User> bookOwners = bookService.getBookOwnersOfBook(bookId);
        if (bookOwners.isEmpty()) {
            return noContent().build();
        }
        return ok(bookOwners);
    }

}
