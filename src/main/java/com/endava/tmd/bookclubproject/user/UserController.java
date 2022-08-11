package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "User")
@RestController
@RequestMapping(path = "users")
public class UserController {

    @Autowired
    private UserService userService;


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET)
    @Operation(
            summary = "Get all users.",
            description = "Getting all users registered within application.",
            responses = {
                    @ApiResponse(
                            description = "See all the users with success.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))
                    ),
                    @ApiResponse(
                            description = "No users are registered.",
                            responseCode = "204",
                            content = @Content
                    )

            }
    )
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> usersList = userService.getAllUsers();
        if (usersList.isEmpty()) {
            return noContent().build();
        }
        return ok(usersList);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET, value = "/BooksOwned")
    @Operation(
            summary = "Get books owned.",
            description = "Get all books owned by a certain user.",
            responses = {
                    @ApiResponse(
                            description = "Get books owned by user.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
                    ),
                    @ApiResponse(
                            description = "User owns no book.",
                            responseCode = "204",
                            content = @Content
                    )

            }
    )
    public ResponseEntity<List<Book>> getBooksOfUser(@RequestParam("userId") final Long userId) {
        List<Book> booksOwned = userService.getBooksOfUser(userId);
        if (booksOwned.isEmpty()) {
            return noContent().build();
        }
        return ok(booksOwned);
    }

    @RequestMapping(method = RequestMethod.POST)
    @Operation(
            summary = "Register new user.",
            description = "Enter data for a new account and create it, if username and email not already used.",
            responses = {
                    @ApiResponse(
                            description = "User account created with success.",
                            responseCode = "200",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Username or email already used.",
                            responseCode = "400",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<String> registerUser(@RequestBody final Optional<User> userOptional) {
        userService.registerUser(userOptional);
        return ok("User account created with success!");
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @Operation(
            summary = "Delete User.",
            description = "Delete the user with given id and all of his traces, including borrows he made, books he owns etc.",
            responses = {
                    @ApiResponse(
                            description = "User account and every action he made deleted with success.",
                            responseCode = "200",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "User with given id does not exist.",
                            responseCode = "400",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<String> deleteUser(@RequestParam("userId") final Long userId) {
        userService.deleteUser(userId);
        return ok("User with id " + userId + " and all his work deleted!");
    }

}
