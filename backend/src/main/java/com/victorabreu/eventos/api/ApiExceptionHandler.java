package com.victorabreu.eventos.api;

import com.victorabreu.eventos.domain.DuplicateEventException;
import com.victorabreu.eventos.domain.EventNotFoundException;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(DuplicateEventException.class)
    public ProblemDetail duplicate(DuplicateEventException exception) {
        return problem(HttpStatus.CONFLICT, "Evento duplicado", exception.getMessage());
    }
    @ExceptionHandler(EventNotFoundException.class)
    public ProblemDetail missing(EventNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Evento não encontrado", exception.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException exception) {
        var result = problem(HttpStatus.BAD_REQUEST, "Dados inválidos", "Revise os campos informados.");
        var errors = new TreeMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        result.setProperty("errors", errors);
        return result;
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, HandlerMethodValidationException.class})
    public ProblemDetail malformed(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", "Verifique o JSON, o tipo, a data/hora e os parâmetros de paginação.");
    }
    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpected(Exception exception) {
        if (exception instanceof org.springframework.web.ErrorResponse error) {
            var status = HttpStatus.valueOf(error.getStatusCode().value());
            return switch (status) {
                case NOT_FOUND -> problem(status, "Recurso não encontrado", "O endereço solicitado não foi encontrado.");
                case METHOD_NOT_ALLOWED -> problem(status, "Método não permitido", "O método HTTP informado não é permitido neste endereço.");
                case UNSUPPORTED_MEDIA_TYPE -> problem(status, "Formato não suportado", "Envie o corpo da requisição no formato JSON (application/json).");
                case NOT_ACCEPTABLE -> problem(status, "Formato de resposta não disponível", "Solicite uma resposta no formato JSON.");
                default -> problem(status, "Não foi possível atender à requisição", "Verifique o endereço e os parâmetros informados.");
            };
        }
        LOG.error("Falha inesperada ao processar a requisição", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Não foi possível concluir a operação. Tente novamente.");
    }
    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        var result = ProblemDetail.forStatusAndDetail(status, detail);
        result.setTitle(title);
        return result;
    }
}
