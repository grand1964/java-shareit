package ru.practicum.shareit.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.common.convert.ListConverter;
import ru.practicum.shareit.common.convert.PairToReturn;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class ItemJpaTests {
    private static User ownerBase;
    private static User authorBase;
    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeAll
    public static void setUp() {
        ownerBase = new User(null, "Vasya", "vasya@com");
        authorBase = new User(null, "Petya", "petya@com");
    }

    @AfterEach
    public void clearAll() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    public void searchItemTest() {
        //создаем владельца
        User owner = userRepository.save(ownerBase);
        //создаем несколько его вещей
        itemRepository.save(
                new Item(null, "notebook", "ASUS", true, owner, null));
        itemRepository.save(
                new Item(null, "notebook", "Samsung", false, owner, null));
        itemRepository.save(
                new Item(null, "TeXBook", "By Knuth", true, owner, null));
        itemRepository.save(new Item(null, "About Spring",
                "The book about java", true, owner, null));
        itemRepository.save(
                new Item(null, "computer", "Apple", true, owner, null));
        //выполняем поиск
        PageRequest pageable = PageRequest.of(0, 20);
        List<Item> items = itemRepository.searchItems("bOok", pageable).getContent();
        //проверяем результат
        assertEquals(items.size(), 3);
        assertEquals(items.get(0).getName(), "notebook");
        assertEquals(items.get(1).getDescription(), "By Knuth");
        assertEquals(items.get(2).getName(), "About Spring");
    }

    @Test
    public void getAllCommentsByOwnerTest() {
        //создаем владельца
        User owner = userRepository.save(ownerBase);
        //создаем автора комментария
        User author = userRepository.save(authorBase);

        //создаем вещь для комментария
        Item item = new Item(null, "notebook", "ASUS", true, owner, null);
        itemRepository.save(item);
        //создаем два комментария к вещи
        Comment comment1 = commentRepository.save(
                new Comment(null, "Good", Timestamp.from(Instant.now()), item, author));
        Comment comment2 = commentRepository.save(
                new Comment(null, "Bad", Timestamp.from(Instant.now()), item, author));
        //читаем комментарии
        List<PairToReturn<Item, Comment>> commentPairs = itemRepository.getAllCommentsByOwner(owner.getId());
        //проверяем их количество
        assertEquals(commentPairs.size(), 2);
        //преобразуем их в отображение
        Map<Item, List<Comment>> map = ListConverter.keyToValues(commentPairs);
        //проверяем результат
        assertTrue(map.containsKey(item));
        assertTrue(map.get(item).contains(comment1));
        assertTrue(map.get(item).contains(comment2));
    }
}
