package ru.yandex.practicum.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.practicum.payment.model.ErrorResponse;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({PaymentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    private ErrorResponse buildError(final HttpStatus httpStatus, final String message) {
        return new ErrorResponse()
                .timestamp(OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()))
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(message);
    }
}
