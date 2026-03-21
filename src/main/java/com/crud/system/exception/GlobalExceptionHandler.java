package com.crud.system.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler global de exceções da API.
 * Retorna respostas padronizadas sem expor stack traces pro cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Recurso não encontrado - 404. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Recurso não encontrado: {}", ex.getMessage());

        ErrorResponse error = buildErrorResponse(
            HttpStatus.NOT_FOUND.value(), "Não Encontrado",
            ex.getMessage(), extractPath(request), null
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /** Erro de validação do @Valid - 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        logger.warn("Erro de validação: {}", errors);

        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Erro de Validação",
            "Erro de validação nos dados fornecidos", extractPath(request), errors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /** Violação de constraint do Bean Validation - 400. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();
        ex.getConstraintViolations().forEach(v ->
            errors.add(v.getPropertyPath() + ": " + v.getMessage())
        );
        logger.warn("Violação de constraint: {}", errors);

        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Violação de Constraint",
            "Violação de restrições nos dados fornecidos", extractPath(request), errors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /** Regra de negócio violada - 422. */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleException(
            BusinessRuleException ex, WebRequest request) {
        logger.warn("Regra de negócio violada: {}", ex.getMessage());

        ErrorResponse error = buildErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(), "Violação de Regra de Negócio",
            ex.getMessage(), extractPath(request), null
        );
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /** Argumento inválido - 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        logger.warn("Argumento inválido: {}", ex.getMessage());

        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Argumento Inválido",
            ex.getMessage(), extractPath(request), null
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /** JSON malformado na requisição - 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        logger.warn("JSON malformado: {}", ex.getMessage());

        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "JSON Malformado",
            "O corpo da requisição está malformado ou não pode ser processado",
            extractPath(request), null
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /** Tipo de parâmetro incompatível (ex: string no lugar de número) - 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        logger.warn("Tipo incompatível: {}", ex.getMessage());

        String message = String.format("O parâmetro '%s' deveria ser do tipo %s",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido"
        );

        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Tipo de Parâmetro Inválido",
            message, extractPath(request), null
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /** Formato numérico inválido - 400. */
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ErrorResponse> handleNumberFormatException(
            NumberFormatException ex, WebRequest request) {
        logger.warn("Formato numérico inválido: {}", ex.getMessage());

        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(), "Formato Numérico Inválido",
            "O formato do número fornecido é inválido", extractPath(request), null
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /** Handler genérico - não expõe detalhes internos pro cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        logger.error("Erro interno do servidor", ex);

        ErrorResponse error = buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro Interno do Servidor",
            "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.",
            extractPath(request), null
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /** Extrai o path da requisição. */
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    /** Monta o ErrorResponse padronizado. */
    private ErrorResponse buildErrorResponse(int status, String error, String message, String path, List<String> details) {
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(), status, error, message, path
        );
        if (details != null && !details.isEmpty()) {
            response.setDetails(details);
        }
        return response;
    }
}
