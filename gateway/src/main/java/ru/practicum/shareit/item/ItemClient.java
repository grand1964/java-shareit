package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.common.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemInDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    ///////////////////////////// Получение данных ///////////////////////////

    //получение всех вещей
    public ResponseEntity<Object> getAllItems(long ownerId, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", ownerId, parameters);
    }

    //получение вещи по идентификатору
    public ResponseEntity<Object> getItem(long itemId, long userId) {
        return get("/" + itemId, userId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    //создание новой вещи
    public ResponseEntity<Object> createItem(long ownerId, ItemInDto itemInDto) {
        return post("", ownerId, itemInDto);
    }

    //редактирование вещи
    public ResponseEntity<Object> patchItem(long itemId, long ownerId, ItemInDto itemInDto) {
        itemInDto.setId(itemId);
        return patch("/" + itemId, ownerId, itemInDto);
    }

    //создание комментария
    public ResponseEntity<Object> createComment(long itemId, long authorId, CommentInDto commentInDto) {
        return post("/" + itemId + "/comment", authorId, commentInDto);
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    public ResponseEntity<Object> deleteItem(long id) {
        return delete("/" + id);
    }

    public ResponseEntity<Object> deleteAllItems() {
        return delete("");
    }

    ////////////////////////////////// Поиск /////////////////////////////////

    public ResponseEntity<Object> searchItems(String text, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", null, parameters);
    }
}
