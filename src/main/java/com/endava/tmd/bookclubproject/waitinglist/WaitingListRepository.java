package com.endava.tmd.bookclubproject.waitinglist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@SuppressWarnings("SqlDialectInspection")
@Repository
public interface WaitingListRepository extends JpaRepository<WaitingList, Long> {

    Optional<WaitingList> findByBookIdAndUserId(final Long bookId, final Long userId);
    @Transactional
    void deleteAllByUserId(final Long userId);
    @Transactional
    void deleteAllByBookId(final Long bookId);
}
