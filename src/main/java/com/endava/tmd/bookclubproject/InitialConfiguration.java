package com.endava.tmd.bookclubproject;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import com.endava.tmd.bookclubproject.waitinglist.WaitingList;
import com.endava.tmd.bookclubproject.waitinglist.WaitingListRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.util.List;

import static com.endava.tmd.bookclubproject.security.UserRoles.ADMIN;
import static com.endava.tmd.bookclubproject.security.UserRoles.USER;

@Configuration
public class InitialConfiguration {

    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository,
                                        BookRepository bookRepository,
                                        BookOwnerRepository bookOwnerRepository,
                                        BookBorrowerRepository bookBorrowerRepository,
                                        WaitingListRepository waitingListRepository) {

        return args -> {
            List<User> initialUsers = createInitialUsers();
            List<Book> initialBooks = createInitialBooks();
            List<BookOwner> initialBookOwners = createInitialBookOwners();
            List<BookBorrower> initialBookBorrowers = createInitialBookBorrowers();
            List<WaitingList> initialWaitingList = createInitialWaitingList();

            int strength = 10;
            for (User user : initialUsers) {
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(strength, new SecureRandom());
                String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
                user.setPassword(encodedPassword);
            }

            userRepository.saveAll(initialUsers);
            bookRepository.saveAll(initialBooks);
            bookOwnerRepository.saveAll(initialBookOwners);
            bookBorrowerRepository.saveAll(initialBookBorrowers);
            waitingListRepository.saveAll(initialWaitingList);
        };
    }


    private List<User> createInitialUsers() {
        User user1 = new User(
                "Mihai",
                "Mitican",
                "mitican123",
                "asd",
                "mitican@endava.com",
                ADMIN
        );
        user1.setId(1L);
        user1.setEnabled(true);

        User user2 = new User(
                "Ionut",
                "Pintea",
                "ionut345",
                "asd",
                "pintea@endava.com",
                USER
        );
        user2.setId(2L);
        user2.setEnabled(true);


        User user3 = new User(
                "Andi",
                "Moisescu",
                "moisea32",
                "asd",
                "moisea@endava.com",
                USER
        );
        user3.setId(3L);
        user3.setEnabled(true);

        User user4 = new User(
                "Andrada",
                "Giurgiu",
                "andraG",
                "asd",
                "andra@endava.com",
                USER
        );
        user4.setId(4L);
        user4.setEnabled(true);

        return List.of(user1, user2, user3, user4);
    }

    private List<Book> createInitialBooks() {
        Book book1 = new Book(
                "Ion",
                "Liviu Rebreanu",
                "I"
        );

        Book book2 = new Book(
                "Limitless",
                "Jim Kwik",
                "I"
        );

        Book book3 = new Book(
                "Ion",
                "Liviu Rebreanu",
                "II"
        );

        Book book4 = new Book(
                "Atomic Habits",
                "James Clear",
                "I"

        );
        book1.setId(1L);
        book2.setId(2L);
        book3.setId(3L);
        book4.setId(4L);
        return List.of(book1, book2, book3, book4);
    }

    private List<BookOwner> createInitialBookOwners() {
        List<User> initialUsers = createInitialUsers();
        List<Book> initialBooks = createInitialBooks();

        return List.of(
                new BookOwner(initialBooks.get(1), initialUsers.get(0)),
                new BookOwner(initialBooks.get(0), initialUsers.get(2)),
                new BookOwner(initialBooks.get(2), initialUsers.get(2)),
                new BookOwner(initialBooks.get(3), initialUsers.get(1))
        );
    }

    private List<BookBorrower> createInitialBookBorrowers() {
        List<User> initialUsers = createInitialUsers();
        List<Book> initialBooks = createInitialBooks();

        return List.of(
                new BookBorrower(
                        initialBooks.get(1),
                        initialUsers.get(3),
                        1L,
                        2L
                )
        );
    }

    private List<WaitingList> createInitialWaitingList(){
        List<User> initialUsers = createInitialUsers();
        List<Book> initialBooks = createInitialBooks();

        return List.of(
                new WaitingList(
                        initialBooks.get(1).getId(),
                        initialUsers.get(0).getId(),
                        initialUsers.get(2).getId()
                ),
                new WaitingList(
                        initialBooks.get(1).getId(),
                        initialUsers.get(0).getId(),
                        initialUsers.get(1).getId()
                )
        );
    }
}
