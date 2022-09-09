package com.endava.tmd.bookclubproject.book;

import com.endava.tmd.bookclubproject.exception.ApiNotFoundException;
import com.endava.tmd.bookclubproject.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasAuthority('book:read')")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            return noContent().build();
        }
        return ok(books);
    }

    @Operation(
            summary = "Get book.",
            description = "Getting book by id.",
            responses = {
                    @ApiResponse(
                            description = "See book with success.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
                    ),
                    @ApiResponse(
                            description = "Book not found.",
                            responseCode = "404",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('book:read')")
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public Book getBookById(@PathVariable("id") final Long bookId){
        return bookService.getBookById(bookId)
                .orElseThrow(() -> new ApiNotFoundException(String.format("There is no book with id %d.", bookId)));
    }


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
    @PreAuthorize("hasAuthority('book:read')")
    @RequestMapping(method = RequestMethod.GET, value = "/availableBooks")
    public ResponseEntity<String> getAllAvailableBooks() {
        List<Book> availableBooks = bookService.getAllAvailableBooks();
        if (availableBooks.isEmpty()) {
            return noContent().build();
        }
        String message = bookService.formatAvailableBooks(availableBooks);
        return ok(message);
    }


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
    @PreAuthorize("hasAuthority('book:read')")
    @RequestMapping(method = RequestMethod.GET, value = "/TitleOrAuthor")
    public ResponseEntity<String> getBooksByTitleOrAuthor(@RequestParam(value = "title", required = false) final String title,
                                                          @RequestParam(value = "author",required = false) final String author)
    {
        List<Book> booksByTitleOrAuthor = bookService.getBooksByTitleOrAuthor(title, author);
        if (booksByTitleOrAuthor.isEmpty()) {
            return noContent().build();
        }
        String message = bookService.formatBooksByTitleOrAuthor(booksByTitleOrAuthor);
        return ok(message);
    }

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
                            responseCode = "404",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('user:read')")
    @RequestMapping(method = RequestMethod.GET, value = "/BookOwners")
    public ResponseEntity<List<User>> getBookOwnersOfBook(@RequestParam("bookId") final Long bookId) {
        Optional<Book> bookOptional = bookService.getBookById(bookId);
        if(bookOptional.isEmpty()){
            throw new ApiNotFoundException(String.format("There is no book with id %d.", bookId));
        }

        List<User> bookOwners = bookService.getBookOwnersOfBook(bookId);
        return ok(bookOwners);
    }

}
