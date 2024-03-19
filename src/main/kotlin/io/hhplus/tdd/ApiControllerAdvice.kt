package io.hhplus.tdd

import io.hhplus.tdd.error.NotEnoughPointException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

data class ErrorResponse(val code: String, val message: String)

@RestControllerAdvice
class ApiControllerAdvice : ResponseEntityExceptionHandler() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("500", "에러가 발생했습니다."),
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }

    @ExceptionHandler(NotEnoughPointException::class)
    fun handleNotEnoughPointException(e: NotEnoughPointException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", "포인트가 부족합니다."),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", "잘못된 요청입니다."),
            HttpStatus.BAD_REQUEST,
        )
    }
}