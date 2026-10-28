package org.example.exception;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class CustomGlobalExceptionHandler {

    // 404
    @ExceptionHandler({
            EntityNotFoundException.class,
            jakarta.persistence.EntityNotFoundException.class})
    public ResponseEntity<String> handleNotFound(RuntimeException e) {
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleValidationException(
            MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getAllErrors().stream()
                .map(objectError -> objectError.getDefaultMessage())
                .toList();
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            IncorrectPasswordException.class,
            InvalidTelegramToken.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleUnreadableBody(HttpMessageNotReadableException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Invalid value for parameter '" + e.getName() + "'");
    }

    // 401
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthentication(AuthenticationException e) {
        return buildResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // 402
    @ExceptionHandler(PaymentNotConfirmedException.class)
    public ResponseEntity<String> handlePaymentNotConfirmed(PaymentNotConfirmedException e) {
        return buildResponse(HttpStatus.PAYMENT_REQUIRED, e.getMessage());
    }

    // 403
    @ExceptionHandler({
            AccessDeniedException.class,
            UnauthorizedBookingAccessException.class,
            org.springframework.security.access.AccessDeniedException.class})
    public ResponseEntity<String> handleAccessDenied(RuntimeException e) {
        return buildResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    // 409
    @ExceptionHandler({
            RegistrationException.class,
            BookingConflictException.class,
            AccommodationUnavailableException.class,
            BookingPaymentNotAllowedException.class})
    public ResponseEntity<String> handleConflict(RuntimeException e) {
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return buildResponse(HttpStatus.CONFLICT, "Data integrity violation");
    }

    // 502
    @ExceptionHandler(StripePaymentSessionException.class)
    public ResponseEntity<String> handleStripe(StripePaymentSessionException e) {
        log.error("Stripe error", e);
        return buildResponse(HttpStatus.BAD_GATEWAY, e.getMessage());
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception e) {
        log.error("Unexpected error", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<String> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(message);
    }
}
