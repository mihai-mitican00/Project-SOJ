package com.endava.tmd.bookclubproject.bookborrower;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@SuppressWarnings("JpaDataSourceORMInspection")
@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "book_borrowers")
public class BookBorrower {

    @EmbeddedId
    private BookBorrowerId bookBorrowerId;

    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @MapsId("borrowerId")
    @JoinColumn(name = "borrower_id")
    private User borrower;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    public BookBorrower(final Book book,final User borrower,final Long ownerId,final Long weeksToRent){
        this.bookBorrowerId = new BookBorrowerId(book.getId(), borrower.getId());
        this.ownerId = ownerId;
        this.book = book;
        this.borrower = borrower;
        this.borrowDate = LocalDate.now();
        this.returnDate = borrowDate.plusWeeks(weeksToRent);
    }


    public String toStringBorrowerFocused(){
        return this.book.toString()
                + "\nReturn date: " + this.getReturnDate()
                + "\nBorrower id: " + this.borrower.getId()
                + "\nBorrower username: " + this.borrower.getUsername()
                + "\nBorrower email: " + this.borrower.getEmail()
                + "\nOwner id: " + this.getOwnerId();
    }

    public String toStringOwnerFocused(){
        return this.book.toString()
                + "\nReturn date: " + this.getReturnDate()
                + "\nOwner id: " + this.getOwnerId()
                + "\nBorrower id: " + this.borrower.getId();
    }
}
