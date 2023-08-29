package ru.practicum.shareit.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.common.convert.PairToReturn;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    //поиск всех пар (запрос, результат) одного автора
    @Query("select r as key, i as value from Item i " +
            "right join i.request r " +
            "where r.requester.id = ?1 "
    )
    List<PairToReturn<ItemRequest, Item>> getAllRequestPairsByOwner(Long ownerId);

    //поиск всех пар (запрос, результат) других пользователей
    @Query("select r as key, i as value from Item i " +
            "right join i.request r " +
            "where r.requester.id <> ?1 and r in ?2 "
    )
    List<PairToReturn<ItemRequest, Item>> getAllRequestPairs(long userId, List<ItemRequest> items);
}
