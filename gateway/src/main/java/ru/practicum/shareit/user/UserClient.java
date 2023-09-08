package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.common.client.BaseClient;
import ru.practicum.shareit.user.dto.UserInDto;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    ///////////////////////////// Получение данных ///////////////////////////

    public ResponseEntity<Object> getAllUsers() {
        return get("");
    }

    public ResponseEntity<Object> getUser(long userId) {
        return get("/" + userId);
    }

    /////////////////////////// Создание и обновление ////////////////////////

    //создание нового пользователя
    public ResponseEntity<Object> createUser(UserInDto userInDto) {
        return post("", userInDto);
    }

    public ResponseEntity<Object> patchUser(long userId, UserInDto userInDto) {
        return patch("/" + userId, userId, userInDto);
    }

    ///////////////////////////////// Удаление ///////////////////////////////

    public ResponseEntity<Object> deleteUser(long id) {
        return delete("/" + id, id);
    }

    public ResponseEntity<Object> deleteAllUsers() {
        return delete("");
    }
}
