package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.sql.Timestamp;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwner_Id(long ownerId);

    @Query("select i from Item i " +
            "where i.available = true and " +
            "(lower(i.name) like lower(concat('%', ?1, '%')) " +
            "or lower(i.description) like lower(concat('%', ?1, '%'))) " +
            "order by i.id asc "
    )
    List<Item> searchItems(String sample);

    //последнее бронирование заданной вещи
    @Query("select b from Booking b join b.item i " +
            "where i.id = ?1 and b.status = 'APPROVED' and " +
            "b.end = (select max(bb.end) from Booking bb where bb.item = i and bb.start < ?2) "
    )
    List<Booking> getLastBookingOfItem(Long itemId, Timestamp now);

    //ближайшее бронирование заданной вещи
    @Query("select b from Booking b join b.item i " +
            "where i.id = ?1 and b.status = 'APPROVED' and " +
            "b.start = (select min(bb.start) from Booking bb where bb.item = i and bb.start > ?2) "
    )
    List<Booking> getNextBookingOfItem(Long itemId, Timestamp now);

    //все старые бронирования вещей одного владельца
    @Query("select i, b from Booking b join b.item i " +
            "where i.owner.id = ?1 and b.status = 'APPROVED' and " +
            "b.end = (select max(bb.end) from Booking bb where bb.item = i and bb.start < ?2) "
    )
    List<Object[]> getAllLastBookingsByOwner(Long ownerId, Timestamp now);

    //все будущие бронирования вещей одного владельца
    @Query("select i, b from Booking b join b.item i " +
            "where i.owner.id = ?1 and b.status = 'APPROVED' and " +
            "b.start = (select min(bb.start) from Booking bb where bb.item = i and bb.start > ?2) "
    )
    List<Object[]> getAllNextBookingsByOwner(Long ownerId, Timestamp now);

    //все отзывы на все вещи одного владельца
    @Query("select i, c from Comment c " +
            "join c.item i " +
            "where i.owner.id = ?1 "
    )
    List<Object[]> getAllCommentsByOwner(Long ownerId);
}
