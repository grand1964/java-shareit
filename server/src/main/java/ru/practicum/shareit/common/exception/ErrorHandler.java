package ru.practicum.shareit.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice(basePackages = "ru.practicum.shareit")
public class ErrorHandler {
    @ExceptionHandler({BadRequestException.class,
            MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidate(RuntimeException e) {
        String message = e.getMessage();
        log.error(message);
        return new ErrorResponse(message, "400");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        String message = e.getMessage();
        log.error(message);
        return new ErrorResponse(message, "404");
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(RuntimeException e) {
        String message = e.getMessage();
        log.error(message);
        return new ErrorResponse(message, "409");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenUser(ForbiddenException e) {
        String message = e.getMessage();
        log.error(message);
        return new ErrorResponse(message, "403");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedError(Throwable e) {
        String message = "Произошла непредвиденная ошибка";
        log.error(message);
        return new ErrorResponse(message + ": " + e.getCause(), "500");
    }
}
