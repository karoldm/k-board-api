package com.karoldm.k_board_api.handlers;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karoldm.k_board_api.dto.response.ErrorResponseDTO;
import com.karoldm.k_board_api.exceptions.AmazonS3Exception;
import com.karoldm.k_board_api.exceptions.InvalidPasswordException;
import com.karoldm.k_board_api.exceptions.UserNotAuthenticated;
import com.karoldm.k_board_api.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {

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

    @ExceptionHandler(UserNotAuthenticated.class)
    private ResponseEntity<ErrorResponseDTO> handleUserNotAuthenticated(UserNotAuthenticated ex) {
        ErrorResponseDTO errorObject = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler({
            BadCredentialsException.class,
            AuthenticationException.class,
            InternalAuthenticationServiceException.class
    })
    private ResponseEntity<ErrorResponseDTO> handleAuthenticationException(Exception ex) {
        ErrorResponseDTO errorObject = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid email or password."
        );

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    private ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
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
    private ResponseEntity<ErrorResponseDTO> handleTokenIsMissingException(JWTDecodeException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid token: " + ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResponseStatusException.class)
    private ResponseEntity<ErrorResponseDTO> handleResponseStatusException(ResponseStatusException ex) {
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
    private ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){

        ErrorResponseDTO errorObject = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException ex){
        ErrorResponseDTO errorObject = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AmazonS3Exception.class)
    private ResponseEntity<ErrorResponseDTO> amazonS3Exception(AmazonS3Exception ex) {
        ErrorResponseDTO errorObject = new ErrorResponseDTO(
                HttpStatus.BAD_GATEWAY.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    private ResponseEntity<ErrorResponseDTO> invalidPasswordException(InvalidPasswordException ex) {
        ErrorResponseDTO errorObject = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }
}
