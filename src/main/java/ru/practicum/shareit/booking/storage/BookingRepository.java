package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.sql.Timestamp;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    ///////////////////////// Бронирования по автору ////////////////////////

    //ALL по автору
    List<Booking> findByBooker_IdOrderByStartDesc(Long itemId);

    //PAST по автору
    List<Booking> findByBooker_IdAndEndIsBeforeOrderByStartDesc(Long bookerId, Timestamp end);

    //FUTURE по автору
    List<Booking> findByBooker_IdAndStartIsAfterOrderByStartDesc(Long bookerId, Timestamp start);

    //CURRENT по автору
    List<Booking> findByBooker_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
            Long bookerId, Timestamp start, Timestamp end);

    //WAITING, REJECTED по автору
    List<Booking> findByBooker_IdAndStatusIsOrderByStartDesc(Long itemId, Status status);

    ////////////////////// Бронирования вещей владельца /////////////////////

    //получение бронирований всех вещей
    @Query("select b from Booking b inner join b.item i " +
            "where i.owner.id = ?1 " +
            "order by b.start desc ")
    List<Booking> findAllBookingsForOwner(Long ownerId);

    //получение PAST-бронирований всех вещей
    @Query("select b from Booking b inner join b.item i " +
            "where i.owner.id = ?1 and b.end < ?2 " +
            "order by b.start desc ")
    List<Booking> findPastBookingsForOwner(Long ownerId, Timestamp end);

    //получение FUTURE-бронирований всех вещей
    @Query("select b from Booking b inner join b.item i " +
            "where i.owner.id = ?1 and b.start > ?2 " +
            "order by b.start desc ")
    List<Booking> findFutureBookingsForOwner(Long ownerId, Timestamp start);

    //получение CURRENT-бронирований всех вещей
    @Query("select b from Booking b inner join b.item i " +
            "where i.owner.id = ?1 and b.start < ?2 and b.end > ?2 " +
            "order by b.start desc ")
    List<Booking> findCurrentBookingsForOwner(Long ownerId, Timestamp middle);

    //получение WAITING- и REJECTED-бронирований всех вещей
    @Query("select b from Booking b inner join b.item i " +
            "where i.owner.id = ?1 and b.status = ?2 " +
            "order by b.start desc ")
    List<Booking> findStatusBookingsForOwner(Long ownerId, Status status);

    /////////////////////// Поддержка поиска наложений //////////////////////

    //поиск истекших бронирований
    List<Booking> findByBooker_IdAndEndBeforeAndStatusIs(Long bookerId, Timestamp now, Status status);

    //поиск для вещи бронирований с заданным статусом, налегающих на заданный промежуток
    List<Booking> findByItem_IdAndEndAfterAndStartBeforeAndStatusIs(
            Long bookerId, Timestamp down, Timestamp up, Status status);
}
