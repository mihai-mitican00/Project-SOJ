package com.endava.tmd.bookclubproject.waitinglist;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("waiting_list")
public class WaitingListController {

    @Autowired
    private WaitingListService waitingListService;

    @RequestMapping(method = RequestMethod.GET)
    public Object getAllOnWaitingList() {
        List<WaitingList> listOfEntries = waitingListService.getAllOnWaitingList();
        if (listOfEntries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return listOfEntries;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> addUserOnList(@RequestParam("bookId") Optional<Long> bookId,
                                                @RequestParam("userId") Optional<Long> userId) {

        if (BooleanUtilities.anyEmptyParameters(bookId, userId)) {
            return HttpResponseUtilities.wrongParameters();
        }

        return waitingListService.addUserOnList(bookId.orElse(null), userId.orElse(null));
    }

}
