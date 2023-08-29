package ru.practicum.shareit.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.common.convert.ListConverter;
import ru.practicum.shareit.common.convert.PairToReturn;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class ItemRequestJpaTests {
    private static User ownerBase;
    private static User requesterBase;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    @BeforeAll
    public static void setUp() {
        ownerBase = new User(null, "Vasya", "vasya@com");
        requesterBase = new User(null, "Petya", "petya@com");
    }

    @AfterEach
    public void clearAll() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        requestRepository.deleteAll();
    }

    @Test
    public void getAllRequestsByOwnerTest() {
        //создаем владельца
        User owner = userRepository.save(ownerBase);
        //создаем автора запроса
        User requester = userRepository.save(requesterBase);
        //создаем несколько запросов и вещи к ним
        ItemRequest request1 = requestRepository.save(new ItemRequest(
                null, "Request1", requester, Timestamp.from(Instant.now())));
        itemRepository.save(
                new Item(null, "notebook", "ASUS", true, owner, request1));
        ItemRequest request2 = requestRepository.save(new ItemRequest(
                null, "Request2", requester, Timestamp.from(Instant.now())));
        itemRepository.save(
                new Item(null, "notebook", "Samsung", true, owner, request2));
        ItemRequest request3 = requestRepository.save(new ItemRequest(
                null, "Request3", requester, Timestamp.from(Instant.now())));
        itemRepository.save(
                new Item(null, "TeXBook", "By Knuth", true, owner, request3));
        ItemRequest request4 = requestRepository.save(new ItemRequest(
                null, "Request4", requester, Timestamp.from(Instant.now())));
        itemRepository.save(new Item(null, "About Spring",
                "The book about java", true, owner, request4));
        //читаем запросы автора с результатами
        List<PairToReturn<ItemRequest, Item>> requestPairs =
                requestRepository.getAllRequestPairsByOwner(requester.getId());
        //проверяем их количество
        assertEquals(requestPairs.size(), 4);
        //преобразуем их в отображение
        Map<ItemRequest, List<Item>> map = ListConverter.keyToValues(requestPairs);
        //проверяем результат
        assertTrue(map.containsKey(request1));
        assertEquals(map.get(request1).size(), 1);
        assertEquals(map.get(request1).get(0).getDescription(), "ASUS");
        assertEquals(map.get(request2).size(), 1);
        assertEquals(map.get(request2).get(0).getDescription(), "Samsung");
        assertEquals(map.get(request3).size(), 1);
        assertEquals(map.get(request3).get(0).getName(), "TeXBook");
        assertEquals(map.get(request4).size(), 1);
        assertEquals(map.get(request4).get(0).getName(), "About Spring");
    }

    @Test
    public void getAllRequestsByOtherUsersTest() {
        //создаем владельца
        User owner = userRepository.save(ownerBase);
        //создаем автора запроса
        User requester = userRepository.save(requesterBase);
        //создаем список запросов
        List<ItemRequest> requests = new ArrayList<>();
        //создаем несколько запросов и вещи к ним, запоминаем запросы в списке
        ItemRequest request1 = requestRepository.save(new ItemRequest(
                null, "Request1", requester, Timestamp.from(Instant.now())));
        requests.add(request1);
        itemRepository.save(
                new Item(null, "notebook", "ASUS", true, owner, request1));
        ItemRequest request2 = requestRepository.save(new ItemRequest(
                null, "Request2", requester, Timestamp.from(Instant.now())));
        requests.add(request2);
        itemRepository.save(
                new Item(null, "notebook", "Samsung", true, owner, request2));
        ItemRequest request3 = requestRepository.save(new ItemRequest(
                null, "Request3", requester, Timestamp.from(Instant.now())));
        requests.add(request3);
        itemRepository.save(
                new Item(null, "TeXBook", "By Knuth", true, owner, request3));
        ItemRequest request4 = requestRepository.save(new ItemRequest(
                null, "Request4", requester, Timestamp.from(Instant.now())));
        requests.add(request4);
        itemRepository.save(new Item(null, "About Spring",
                "The book about java", true, owner, request4));
        //читаем запросы пользователей, отличных от заданного, с результатами
        List<PairToReturn<ItemRequest, Item>> requestPairs =
                requestRepository.getAllRequestPairs(owner.getId(), requests);
        //проверяем их количество
        assertEquals(requestPairs.size(), 4);
        //преобразуем их в отображение
        Map<ItemRequest, List<Item>> map = ListConverter.keyToValues(requestPairs);
        //проверяем результат
        assertTrue(map.containsKey(request1));
        assertEquals(map.get(request1).size(), 1);
        assertEquals(map.get(request1).get(0).getDescription(), "ASUS");
        assertEquals(map.get(request2).size(), 1);
        assertEquals(map.get(request2).get(0).getDescription(), "Samsung");
        assertEquals(map.get(request3).size(), 1);
        assertEquals(map.get(request3).get(0).getName(), "TeXBook");
        assertEquals(map.get(request4).size(), 1);
        assertEquals(map.get(request4).get(0).getName(), "About Spring");
    }
}
