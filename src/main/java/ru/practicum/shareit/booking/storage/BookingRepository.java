package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    ///////////////////////// Бронирования по автору ////////////////////////

    //ALL по автору
    Page<Booking> findByBooker_IdOrderByStartDesc(Long bookerId, Pageable pageable);

    //PAST по автору
    Page<Booking> findByBooker_IdAndEndIsBeforeOrderByStartDesc(Long bookerId, Timestamp end, Pageable pageable);

    //FUTURE по автору
    Page<Booking> findByBooker_IdAndStartIsAfterOrderByStartDesc(Long bookerId, Timestamp start, Pageable pageable);

    //CURRENT по автору
    Page<Booking> findByBooker_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
            Long bookerId, Timestamp start, Timestamp end, Pageable pageable);

    //WAITING, REJECTED по автору
    Page<Booking> findByBooker_IdAndStatusIsOrderByStartDesc(Long itemId, Status status, Pageable pageable);

    ////////////////////// Бронирования вещей владельца /////////////////////

    //получение бронирований всех вещей
    @Query("select b from Booking b inner join b.item i " +
            "where i.owner.id = ?1 " +
            "order by b.start desc ")
    Page<Booking> findAllBookingsForOwner(Long ownerId, Pageable pageable);

    //получение PAST-бронирований всех вещей
    @Query("select b from Booking b inner join b.item i " +
            "where i.owner.id = ?1 and b.end < ?2 " +
            "order by b.start desc ")
    Page<Booking> findPastBookingsForOwner(Long ownerId, Timestamp end, Pageable pageable);

    //получение FUTURE-бронирований всех вещей
    @Query("select b from Booking b inner join b.item i " +
            "where i.owner.id = ?1 and b.start > ?2 " +
            "order by b.start desc ")
    Page<Booking> findFutureBookingsForOwner(Long ownerId, Timestamp start, Pageable pageable);

    //получение CURRENT-бронирований всех вещей
    @Query("select b from Booking b inner join b.item i " +
            "where i.owner.id = ?1 and b.start < ?2 and b.end > ?2 " +
            "order by b.start desc ")
    Page<Booking> findCurrentBookingsForOwner(Long ownerId, Timestamp middle, Pageable pageable);

    //получение WAITING- и REJECTED-бронирований всех вещей
    @Query("select b from Booking b inner join b.item i " +
            "where i.owner.id = ?1 and b.status = ?2 " +
            "order by b.start desc ")
    Page<Booking> findStatusBookingsForOwner(Long ownerId, Status status, Pageable pageable);

    /////////////////////// Поддержка поиска наложений //////////////////////

    //поиск истекших бронирований
    List<Booking> findByBooker_IdAndEndBeforeAndStatusIs(Long bookerId, Timestamp now, Status status);

    //поиск для вещи бронирований с заданным статусом, налегающих на заданный промежуток
    List<Booking> findByItem_IdAndEndAfterAndStartBeforeAndStatusIs(
            Long itemId, Timestamp down, Timestamp up, Status status);
}
