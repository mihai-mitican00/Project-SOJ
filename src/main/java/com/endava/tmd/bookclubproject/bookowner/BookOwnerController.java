package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("book_owners")
public class BookOwnerController {

    @Autowired
    private BookOwnerService bookOwnerService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BookOwner>> getBooksAndOwners(){
        List<BookOwner> bookOwners = bookOwnerService.getBooksAndOwners();
        if(BooleanUtilities.emptyList(bookOwners)){
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(bookOwners);
    }

    @RequestMapping(method = RequestMethod.POST, params = "userId")
    public ResponseEntity<String> addBookByUserId(@RequestParam final Long userId, @RequestBody final Optional<Book> bookOptional){
        return bookOwnerService.addBookByUserId(userId, bookOptional);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<String> removeBookOwnerEntry(@RequestParam("bookId") final Optional<Long> bookId,
                                                       @RequestParam("userId") final Optional<Long> userId){

        return bookOwnerService.deleteBookFromUser(bookId, userId);

    }

}
