package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "users")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public Object getUsers() {
        List<User> usersList = userService.getUsers();

        if (BooleanUtilities.emptyList(usersList)) {
            return HttpResponseUtilities.noContentFound();
        }
        return usersList;
    }

    @RequestMapping(method = RequestMethod.GET, value = "BooksOwned")
    public Object getBooksOwned(@RequestParam("userId") Long userId){
        List<Book> booksOwned = userService.getBooksOwned(userId);

        if (booksOwned.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return booksOwned;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> registerUser(@RequestBody final Optional<User> userOptional){
        return userService.registerUser(userOptional);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteUser(@RequestParam("userId") final Optional<Long> userId) {
       return userService.deleteUser(userId);
    }

}
