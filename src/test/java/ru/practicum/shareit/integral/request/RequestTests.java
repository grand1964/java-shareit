package ru.practicum.shareit.integral.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.item.dto.ItemOutRequestDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestTests {
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

    ///////////////////////////// Тесты запросов /////////////////////////////

    @Test
    public void normalRequestByIdTest() {
        ItemRequestOutDto request = requestService.get(4, REQUESTER_ID);
        List<ItemOutRequestDto> items = request.getItems();
        assertEquals(items.size(), 4);
    }

    @Test
    public void normalRequestsByOwnerTest() {
        List<ItemRequestOutDto> requests = requestService.getAllByOwner(REQUESTER_ID);
        assertEquals(requests.size(), 4); //всего 4 запроса
        for (int i = 0; i < 4; i++) {
            ItemRequestOutDto request = requests.get(i);
            assertEquals(request.getId(), 4 - i); //порядок - от поздних к ранним
            List<ItemOutRequestDto> items = request.getItems();
            assertEquals(items.size(), 4 - i); //число вещей тоже уменьшается
        }
    }

    @Test
    public void getAllRequestsWithLimitTest() {
        int from = 0;
        int size = 2;
        List<ItemRequestOutDto> requests = requestService.getAll(6, from, size);
        assertEquals(requests.size(), 2); //всего 2 запроса, остальные обрезаны
        for (int i = 0; i < size; i++) {
            ItemRequestOutDto request = requests.get(i);
            assertEquals(request.getId(), 4 - i); //порядок - от поздних к ранним
            List<ItemOutRequestDto> items = request.getItems();
            assertEquals(items.size(), 4 - i); //число вещей тоже уменьшается
        }
    }

    @Test
    public void getAllRequestsWithoutBoundsTest() {
        int from = 0;
        int size = 4;
        List<ItemRequestOutDto> requests = requestService.getAll(6, from, size);
        assertEquals(requests.size(), size); //все запросы должны быть
        for (int i = 0; i < size; i++) {
            ItemRequestOutDto request = requests.get(i);
            assertEquals(request.getId(), size - i); //порядок - от поздних к ранним
            List<ItemOutRequestDto> items = request.getItems();
            assertEquals(items.size(), size - i); //число вещей тоже уменьшается
        }
    }

    @Test
    public void getAllRequestsWithLimitAndOffsetTest() {
        int from = 3;
        int size = 2;
        List<ItemRequestOutDto> requests = requestService.getAll(6, from, size);
        assertEquals(requests.size(), 2); //всего 2 запроса, остальные обрезаны
        for (int i = 0; i < size; i++) {
            ItemRequestOutDto request = requests.get(i);
            assertEquals(request.getId(), 2 - i); //порядок - от поздних к ранним
            List<ItemOutRequestDto> items = request.getItems();
            assertEquals(items.size(), 2 - i); //число вещей тоже уменьшается
        }
    }
}
