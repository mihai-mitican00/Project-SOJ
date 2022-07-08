package com.endava.tmd.bookclubproject.bookowner;

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
public class BookOwnerKey implements Serializable {

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

}
