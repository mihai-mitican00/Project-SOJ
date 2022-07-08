package com.endava.tmd.bookclubproject.bookborrower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@SuppressWarnings("JpaDataSourceORMInspection")
@Data
@NoArgsConstructor
@AllArgsConstructor

@Embeddable
public class BookBorrowerId implements Serializable {

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;
}
