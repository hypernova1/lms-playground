package org.sam.lms.api.common

import org.sam.lms.common.exception.ErrorCode
import org.sam.lms.common.exception.ErrorResult
import org.sam.lms.common.exception.HttpException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException


@RestControllerAdvice
class GlobalExceptionHandler(
    private val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
) {

    @ExceptionHandler(HttpException::class)
    protected fun httpException(ex: HttpException): ResponseEntity<*> {
        return ResponseEntity.status(ex.status).body(ErrorResult(ex.errorCode!!.code, ex.errorCode!!.message, ex.data))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun methodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<*> {
        val allErrors = ex.allErrors

        val errorMessages: MutableList<String?> = ArrayList()
        for (error in allErrors) {
            errorMessages.add(error.defaultMessage)
        }

        return ResponseEntity.unprocessableEntity().body<ErrorResult>(
            ErrorResult(
                ErrorCode.MISMATCH_PARAMETER.code,
                ErrorCode.MISMATCH_PARAMETER.message,
                errorMessages
            )
        )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    protected fun noResourceFoundException(e: NoResourceFoundException?): ResponseEntity<*> {
        return ResponseEntity.notFound().build<Any>()
    }

//    @ExceptionHandler(AuthorizationDeniedException::class)
//    protected fun authorizationDeniedException(e: AuthorizationDeniedException): ResponseEntity<*> {
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResult("4001", "권한이 없습니다."))
//    }

    @ExceptionHandler(Exception::class)
    protected fun exception(e: Exception): ResponseEntity<*> {
        e.printStackTrace()
        log.error(e.message)
        return ResponseEntity.internalServerError().body(ErrorResult("9999", "서버에 오류가 발생했습니다."))
    }

}