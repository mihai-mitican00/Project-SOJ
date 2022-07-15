package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Owners")
@RestController
@RequestMapping("book_owners")
public class BookOwnerController {

    @Autowired
    private BookOwnerService bookOwnerService;

    @RequestMapping(method = RequestMethod.GET)
    @Operation(
            summary = "Get all Book Owner entries.",
            description = "Get all books and their owners, a book with more copies has more owners.",
            responses = {
                    @ApiResponse(
                            description = "See all the books that were added and their owners.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookOwner.class))
                    ),
                    @ApiResponse(
                            description = "There are no books added yet in the virtual shelter by anyone.",
                            responseCode = "204",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<List<BookOwner>> getAllBookOwners() {
        return bookOwnerService.getAllBookOwners();
    }

    @RequestMapping(method = RequestMethod.POST)
    @Operation(
            summary = "Add book as user.",
            description = "Add a book owner entry as a user, the book will appear in Book API too if another user did not already added it.",
            responses = {
                    @ApiResponse(
                            description = "Add a book with success.",
                            responseCode = "201",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Book cannot be added in the virtual shelter due to various problems.",
                            responseCode = "400",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<String> addBookByUserId(@RequestParam("userId") final Long userId, @RequestBody final Optional<Book> bookOptional) {
        return bookOwnerService.addBookByUserId(userId, bookOptional);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @Operation(
            summary = "Remove book from owner.",
            description = "Owner removes a book, and all traces of that book " +
                    "including waiting list entries, borrows, " +
                    "even the book itself if it is the only copy.",
            responses = {
                    @ApiResponse(
                            description = "Owner removed book successfully.",
                            responseCode = "200",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Book could not be removed from various reasons.",
                            responseCode = "400",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<String> removeBookOwnerEntry(@RequestParam("bookId") final Long bookId,
                                                       @RequestParam("userId") final Long userId) {
        return bookOwnerService.deleteBookFromUser(bookId, userId);
    }

}
