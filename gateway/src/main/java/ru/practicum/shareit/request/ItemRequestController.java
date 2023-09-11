package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestInDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
	private final ItemRequestClient requestClient;
	private static final String HEADER_NAME = "X-Sharer-User-Id";

	///////////////////////////// Получение данных ///////////////////////////

	//получение всех собственных запросов
	@GetMapping
	public ResponseEntity<Object> getRequestsByOwner(@Valid @NotNull @RequestHeader(HEADER_NAME) Long ownerId) {
		validateId(ownerId);
		log.info("Требуются все запросы пользователя с идентификатором {}", ownerId);
		return requestClient.getRequestsByOwner(ownerId);
	}

	//постраничное получение запросов всех пользователей
	@GetMapping(value = "/all")
	public ResponseEntity<Object> getAllRequests(@RequestHeader(HEADER_NAME) Long userId,
												 @RequestParam(defaultValue = "0") @PositiveOrZero int from,
												 @RequestParam(defaultValue = "20") @Positive int size) {
		validateId(userId);
		log.info("постраничное получение запросов всех пользователей");
		return requestClient.getAllRequests(userId, from, size);
	}

	//получение запроса по его идентификатору
	@GetMapping(value = "/{requestId}")
	public ResponseEntity<Object> getRequest(@Valid @NotNull @RequestHeader(HEADER_NAME) Long userId,
											 @PathVariable("requestId") long requestId) {
		validateId(userId, requestId);
		log.info("Требуется запрос с идентификатором {}", requestId);
		return requestClient.getRequest(requestId, userId);
	}

	/////////////////////////// Создание и обновление ////////////////////////

	//создание нового запроса
	@PostMapping
	public ResponseEntity<Object> createRequest(@Valid @NotNull @RequestHeader(HEADER_NAME) long requesterId,
												   @Valid @RequestBody ItemRequestInDto requestInDto) {
		validateId(requesterId);
		log.info("Создание нового запроса пользователем с идентификатором " + requesterId);
		return requestClient.createRequest(requesterId, requestInDto);
	}

	///////////////////////// Валидация идентификаторов ///////////////////////

	private void validateId(long... ids) {
		for (long id : ids) {
			if (id <= 0) {
				throw new NotFoundException("Идентификатор должен быть положительным");
			}
		}
	}
}
