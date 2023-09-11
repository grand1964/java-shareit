package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.common.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestInDto;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех собственных запросов
    public ResponseEntity<Object> getRequestsByOwner(long ownerId) {
        return get("", ownerId);
    }

    //постраничное получение запросов всех пользователей
    public ResponseEntity<Object> getAllRequests(long userId, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("/all?from={from}&size={size}", userId, parameters);
    }

    //получение запроса по его идентификатору
    public ResponseEntity<Object> getRequest(long requestId, long userId) {
        return get("/" + requestId, userId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    //создание нового запроса
    public ResponseEntity<Object> createRequest(long requesterId, ItemRequestInDto requestInDto) {
        return post("", requesterId, requestInDto);
    }
}
