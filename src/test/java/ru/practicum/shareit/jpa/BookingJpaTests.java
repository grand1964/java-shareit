package ru.practicum.shareit.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class BookingJpaTests {
    private static User ownerBase;
    private static User bookerBase;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeAll
    public static void setUp() {
        ownerBase = new User(null, "Vasya", "vasya@com");
        bookerBase = new User(null, "Petya", "petya@com");
    }

    @AfterEach
    public void clearAll() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    public void findBookingsForOwnerTest() {
        //создаем владельца
        User owner = userRepository.save(ownerBase);
        //создаем заказчика
        User booker = userRepository.save(bookerBase);
        //создаем две вещи владельца
        Item item1 = itemRepository.save(
                new Item(null, "notebook", "ASUS", true, owner, null));
        Item item2 = itemRepository.save(
                new Item(null, "notebook", "Samsung", false, owner, null));
        //создаем бронирования к первой вещи
        bookingRepository.save(
                new Booking(null,
                        Timestamp.from(Instant.now().minusSeconds(7200)),
                        Timestamp.from(Instant.now().minusSeconds(3600)),
                        item1, booker, Status.APPROVED)
        );
        bookingRepository.save(
                new Booking(null,
                        Timestamp.from(Instant.now().minusSeconds(3600)),
                        Timestamp.from(Instant.now().plusSeconds(3600)),
                        item1, booker, Status.WAITING)
        );
        bookingRepository.save(
                new Booking(null,
                        Timestamp.from(Instant.now().plusSeconds(3600)),
                        Timestamp.from(Instant.now().plusSeconds(7200)),
                        item1, booker, Status.REJECTED)
        );
        //создаем бронирования ко второй вещи
        bookingRepository.save(
                new Booking(null,
                        Timestamp.from(Instant.now().minusSeconds(7200)),
                        Timestamp.from(Instant.now().minusSeconds(3600)),
                        item2, booker, Status.APPROVED)
        );
        bookingRepository.save(
                new Booking(null,
                        Timestamp.from(Instant.now().plusSeconds(3600)),
                        Timestamp.from(Instant.now().plusSeconds(7200)),
                        item2, booker, Status.WAITING)
        );

        //читаем все бронирования владельца
        PageRequest pageable = PageRequest.of(0, 20);
        List<Booking> allBookings = bookingRepository
                .findAllBookingsForOwner(owner.getId(), pageable)
                .getContent();
        //проверяем их количество
        assertEquals(allBookings.size(), 5);

        //читаем прошедшие бронирования владельца
        List<Booking> pastBookings = bookingRepository
                .findPastBookingsForOwner(owner.getId(), Timestamp.from(Instant.now()), pageable)
                .getContent();
        //проверяем их количество
        assertEquals(pastBookings.size(), 2);

        //читаем текущие бронирования владельца
        List<Booking> currentBookings = bookingRepository
                .findCurrentBookingsForOwner(owner.getId(), Timestamp.from(Instant.now()), pageable)
                .getContent();
        //проверяем их количество
        assertEquals(currentBookings.size(), 1);

        //читаем будущие бронирования владельца
        List<Booking> futureBookings = bookingRepository
                .findFutureBookingsForOwner(owner.getId(), Timestamp.from(Instant.now()), pageable)
                .getContent();
        //проверяем их количество
        assertEquals(futureBookings.size(), 2);

        //читаем ожидающие бронирования владельца
        List<Booking> waitingBookings = bookingRepository
                .findStatusBookingsForOwner(owner.getId(), Status.WAITING, pageable)
                .getContent();
        //проверяем их количество
        assertEquals(waitingBookings.size(), 2);

        //читаем отвергнутые бронирования владельца
        List<Booking> rejectedBookings = bookingRepository
                .findStatusBookingsForOwner(owner.getId(), Status.REJECTED, pageable)
                .getContent();
        //проверяем их количество
        assertEquals(rejectedBookings.size(), 1);
    }

    @Test
    public void findBookingsForAuthorTest() {
        //создаем владельца
        User owner = userRepository.save(ownerBase);
        //создаем заказчика
        User booker = userRepository.save(bookerBase);
        //создаем две вещи владельца
        Item item1 = itemRepository.save(
                new Item(null, "notebook", "ASUS", true, owner, null));
        Item item2 = itemRepository.save(
                new Item(null, "notebook", "Samsung", false, owner, null));
        //создаем бронирования к первой вещи
        bookingRepository.save(
                new Booking(null,
                        Timestamp.from(Instant.now().minusSeconds(7200)),
                        Timestamp.from(Instant.now().minusSeconds(3600)),
                        item1, booker, Status.APPROVED)
        );
        bookingRepository.save(
                new Booking(null,
                        Timestamp.from(Instant.now().minusSeconds(3600)),
                        Timestamp.from(Instant.now().plusSeconds(3600)),
                        item1, booker, Status.WAITING)
        );
        bookingRepository.save(
                new Booking(null,
                        Timestamp.from(Instant.now().plusSeconds(3600)),
                        Timestamp.from(Instant.now().plusSeconds(7200)),
                        item1, booker, Status.REJECTED)
        );
        //создаем бронирования ко второй вещи
        bookingRepository.save(
                new Booking(null,
                        Timestamp.from(Instant.now().minusSeconds(7200)),
                        Timestamp.from(Instant.now().minusSeconds(3600)),
                        item2, booker, Status.APPROVED)
        );
        bookingRepository.save(
                new Booking(null,
                        Timestamp.from(Instant.now().plusSeconds(3600)),
                        Timestamp.from(Instant.now().plusSeconds(7200)),
                        item2, booker, Status.WAITING)
        );

        //читаем все бронирования владельца
        PageRequest pageable = PageRequest.of(0, 20);
        List<Booking> allBookings = bookingRepository
                .findByBooker_IdOrderByStartDesc(booker.getId(), pageable)
                .getContent();
        //проверяем их количество
        assertEquals(allBookings.size(), 5);

        //читаем прошедшие бронирования владельца
        List<Booking> pastBookings = bookingRepository
                .findByBooker_IdAndEndIsBeforeOrderByStartDesc(
                        booker.getId(), Timestamp.from(Instant.now()), pageable)
                .getContent();
        //проверяем их количество
        assertEquals(pastBookings.size(), 2);

        //читаем текущие бронирования владельца
        Timestamp now = Timestamp.from(Instant.now());
        List<Booking> currentBookings = bookingRepository
                .findByBooker_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                        booker.getId(), now, now, pageable)
                .getContent();
        //проверяем их количество
        assertEquals(currentBookings.size(), 1);

        //читаем будущие бронирования владельца
        List<Booking> futureBookings = bookingRepository
                .findByBooker_IdAndStartIsAfterOrderByStartDesc(
                        booker.getId(), Timestamp.from(Instant.now()), pageable)
                .getContent();
        //проверяем их количество
        assertEquals(futureBookings.size(), 2);

        //читаем ожидающие бронирования владельца
        List<Booking> waitingBookings = bookingRepository
                .findByBooker_IdAndStatusIsOrderByStartDesc(booker.getId(), Status.WAITING, pageable)
                .getContent();
        //проверяем их количество
        assertEquals(waitingBookings.size(), 2);

        //читаем отвергнутые бронирования владельца
        List<Booking> rejectedBookings = bookingRepository
                .findByBooker_IdAndStatusIsOrderByStartDesc(booker.getId(), Status.REJECTED, pageable)
                .getContent();
        //проверяем их количество
        assertEquals(rejectedBookings.size(), 1);
    }
}
