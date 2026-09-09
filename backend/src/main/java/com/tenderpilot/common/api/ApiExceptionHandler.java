package com.tenderpilot.common.api;

import com.tenderpilot.common.domain.ApiAuthenticationException;
import com.tenderpilot.common.domain.ConflictException;
import com.tenderpilot.common.domain.ForbiddenOperationException;
import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(
            ConflictException.class
    )
    ResponseEntity<ApiErrorResponse> conflict(

            ConflictException exception,

            HttpServletRequest request

    ) {

        return response(

                HttpStatus.CONFLICT,

                exception.getMessage(),

                request,

                List.of()

        );

    }


    @ExceptionHandler(
            ApiAuthenticationException.class
    )
    ResponseEntity<ApiErrorResponse> authentication(

            ApiAuthenticationException exception,

            HttpServletRequest request

    ) {

        return response(

                HttpStatus.UNAUTHORIZED,

                exception.getMessage(),

                request,

                List.of()

        );

    }


    @ExceptionHandler(
            ForbiddenOperationException.class
    )
    ResponseEntity<ApiErrorResponse> forbidden(

            ForbiddenOperationException exception,

            HttpServletRequest request

    ) {

        return response(

                HttpStatus.FORBIDDEN,

                exception.getMessage(),

                request,

                List.of()

        );

    }


    @ExceptionHandler(
            InvalidRequestException.class
    )
    ResponseEntity<ApiErrorResponse> invalidRequest(

            InvalidRequestException exception,

            HttpServletRequest request

    ) {

        return response(

                HttpStatus.BAD_REQUEST,

                exception.getMessage(),

                request,

                List.of()

        );

    }


    @ExceptionHandler(
            ResourceNotFoundException.class
    )
    ResponseEntity<ApiErrorResponse> notFound(

            ResourceNotFoundException exception,

            HttpServletRequest request

    ) {

        return response(

                HttpStatus.NOT_FOUND,

                exception.getMessage(),

                request,

                List.of()

        );

    }


    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    ResponseEntity<ApiErrorResponse> validation(

            MethodArgumentNotValidException exception,

            HttpServletRequest request

    ) {

        List<FieldViolation> violations =

                exception
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(
                                this::mapFieldError
                        )
                        .toList();


        return response(

                HttpStatus.BAD_REQUEST,

                "Validation failed",

                request,

                violations

        );

    }


    private FieldViolation mapFieldError(
            FieldError error
    ) {

        return new FieldViolation(

                error.getField(),

                error.getDefaultMessage()
                        == null

                        ? "Invalid value"

                        : error.getDefaultMessage()

        );

    }


    private ResponseEntity<ApiErrorResponse> response(

            HttpStatus status,

            String message,

            HttpServletRequest request,

            List<FieldViolation> violations

    ) {

        return ResponseEntity
                .status(
                        status
                )
                .body(
                        new ApiErrorResponse(

                                Instant.now(),

                                status.value(),

                                status.getReasonPhrase(),

                                message,

                                request.getRequestURI(),

                                violations

                        )
                );

    }

}
