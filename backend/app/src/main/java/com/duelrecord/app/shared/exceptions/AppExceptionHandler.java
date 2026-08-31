package com.duelrecord.app.shared.exceptions;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates exceptions into {@link Problem} responses, resolving titles/details from the
 * application's message bundles according to the current request locale
 * ({@link LocaleContextHolder#getLocale()}), which is populated per-request by the configured
 * {@code localeResolver} based on the {@code Accept-Language} header.
 */
@RequiredArgsConstructor
@RestControllerAdvice
public class AppExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppExceptionHandler.class);

    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors();
        var problemFields = fieldErrors.stream().map(FieldError::new).toList();
        var problem = new Problem(HttpStatus.BAD_REQUEST, message("problem.validation.title"), message("problem.validation.detail"), problemFields);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        var problem = new Problem(HttpStatus.BAD_REQUEST, message("problem.malformedBody.title"), message("problem.malformedBody.detail"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        var problem = new Problem(HttpStatus.BAD_REQUEST, message("problem.invalidParameter.title"), message("problem.invalidParameter.detail", ex.getName()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        var problem = new Problem(HttpStatus.METHOD_NOT_ALLOWED, message("problem.methodNotAllowed.title"), message("problem.methodNotAllowed.detail", ex.getMethod()));
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(problem);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex) {
        var problem = new Problem(HttpStatus.NOT_FOUND, message("problem.notFound.title"), message("problem.notFound.detail"));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex) {
        var problem = new Problem(HttpStatus.BAD_REQUEST, message("problem.business.title"), message(ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        var problem = new Problem(HttpStatus.UNAUTHORIZED, message("problem.unauthorized.title"), message("problem.unauthorized.detail"));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        var problem = new Problem(HttpStatus.FORBIDDEN, message("problem.forbidden.title"), message("problem.forbidden.detail"));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    /**
     * Handles the JPA-thrown {@link jakarta.persistence.EntityNotFoundException} (e.g. from
     * {@code EntityManager.getReference}), which always carries a technical, non-localizable
     * message; the response detail is a generic, localized 404 message instead.
     */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<Object> handleJpaEntityNotFoundException(jakarta.persistence.EntityNotFoundException ex) {
        var problem = new Problem(HttpStatus.NOT_FOUND, message("problem.entityNotFound.title"), message("problem.entityNotFound.detail"));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Handles the application's own {@link EntityNotFoundException}, resolving its
     * {@code messageKey}/{@code args} to a localized, entity-specific detail message.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        var problem = new Problem(HttpStatus.NOT_FOUND, message("problem.entityNotFound.title"), message(ex));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<Object> handleUnprocessableEntityException(UnprocessableEntityException ex) {
        var problem = new Problem(HttpStatus.UNPROCESSABLE_CONTENT, message("problem.unprocessableEntity.title"), message(ex));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(problem);
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<Object> handleEntityAlreadyExistsException(EntityAlreadyExistsException ex) {
        var problem = new Problem(HttpStatus.CONFLICT, message("problem.entityAlreadyExists.title"), message(ex));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
        var problem = new Problem(HttpStatus.UNAUTHORIZED, message("problem.unauthorized.title"), message(ex));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        var status = HttpStatus.valueOf(ex.getStatusCode().value());
        var title = status == HttpStatus.UNAUTHORIZED ? message("problem.unauthorized.title") : message("problem.genericError.title");
        var problem = new Problem(status, title, ex.getReason());
        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<Object> handleInternalServerException(InternalServerException ex) {
        LOGGER.error("Internal server error: key={}, args={}", ex.messageKey(), ex.args(), ex);
        var problem = new Problem(HttpStatus.INTERNAL_SERVER_ERROR, message("problem.internalServerError.title"), message(ex));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        LOGGER.error("Internal server error", ex);
        var problem = new Problem(HttpStatus.INTERNAL_SERVER_ERROR, message("problem.genericError.title"), message("problem.genericError.detail"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private String message(LocalizedException ex) {
        return message(ex.messageKey(), ex.args());
    }
}
