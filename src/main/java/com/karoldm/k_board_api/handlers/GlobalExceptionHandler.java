package com.karoldm.k_board_api.handlers;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karoldm.k_board_api.dto.response.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {

        StringBuilder errorsField = new StringBuilder();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errorsField.append(error.getDefaultMessage()).append(System.lineSeparator())
        );

        ErrorResponseDTO errorObject = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                errorsField.toString()
        );

        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(Exception ex) {
        ErrorResponseDTO errorObject = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid email or password."
        );

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() == java.util.UUID.class) {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                    HttpStatus.NOT_FOUND.value(),
                    "Resource not found. Invalid UUID: " + ex.getValue()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid parameters: " + ex.getValue()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({JWTVerificationException.class, JWTDecodeException.class})
    public ResponseEntity<ErrorResponseDTO> handleTokenIsMissingException(JWTDecodeException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid token: " + ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleResponseStatusException(ResponseStatusException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                ex.getStatusCode().value(),
                ex.getMessage()
                        .replace("400 BAD_REQUEST ", "")
                        .replace("404 NOT_FOUND ", "")
                        .replace("403 FORBIDDEN ", "")
                        .replace("\"", "")
        );

        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){

        ErrorResponseDTO errorObject = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        ErrorResponseDTO errorObject = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage()
                + " - " + ex.getCause() + " - " + Arrays.toString(ex.getStackTrace())
        );

        return new ResponseEntity<>(errorObject, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
