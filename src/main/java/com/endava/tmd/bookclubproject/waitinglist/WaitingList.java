package com.endava.tmd.bookclubproject.waitinglist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Data
@NoArgsConstructor

@Entity
@Table(name = "waiting_list")
public class WaitingList {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "user_id", nullable = false)
    private Long userId;


    public WaitingList(Long bookId, Long userId){
        this.bookId = bookId;
        this.userId = userId;
    }
}
