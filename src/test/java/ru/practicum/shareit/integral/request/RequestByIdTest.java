package ru.practicum.shareit.integral.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.item.dto.ItemOutRequestDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestByIdTest {
    private static final int USER_COUNT = 10;
    private static final int REQUEST_COUNT = 4;
    private static final long REQUESTER_ID = 5;
    private final UserService userService;
    private final ItemService itemService;
    private final ItemRequestService requestService;
    private final JdbcTemplate jdbcTemplate;


    @BeforeEach
    public void setUp() throws InterruptedException {
        //создаем пользователей
        for (int i = 1; i <= USER_COUNT; i++) {
            userService.createUser(TestUtils.createUserInDto("user" + i, "user" + i + "@com"));
        }
        //создаем запросы
        for (long i = 1; i <= REQUEST_COUNT; i++) {
            requestService.createRequest(REQUESTER_ID,
                    TestUtils.createRequestDto("request" + i));
            requestService.updateCreated(i, 100 * i); //разносим времена создания запросов
        }
        //создаем вещи
        int ind = 1;
        for (long i = 1; i <= 4; i++) {
            for (int j = 1; j <= i; j++) {
                ind++;
                itemService.createItem(i,
                        TestUtils.createItemDto("item" + ind, "request" + i, true, i));
            }
        }
    }

    @AfterEach
    public void resetData() {
        jdbcTemplate.update(TestUtils.getSqlForReset());
    }


    @Test
    public void normalRequestByIdTest() {
        ItemRequestOutDto request = requestService.get(4, REQUESTER_ID);
        List<ItemOutRequestDto> items = request.getItems();
        assertEquals(items.size(), 4);
    }
}
