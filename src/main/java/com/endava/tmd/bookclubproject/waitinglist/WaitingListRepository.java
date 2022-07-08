package com.endava.tmd.bookclubproject.waitinglist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@SuppressWarnings("SqlDialectInspection")
@Repository
public interface WaitingListRepository extends JpaRepository<WaitingList, Long> {

    @Query(value = "SELECT * FROM waiting_list WHERE book_id = ?1 AND user_id = ?2", nativeQuery = true)
    Optional<WaitingList> getEntryByBookIdAndUserId(final Long bookId, final Long userId);
}
