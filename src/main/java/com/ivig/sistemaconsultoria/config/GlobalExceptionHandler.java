package com.ivig.sistemaconsultoria.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * ============================================================
     * ARGUMENTO INVÁLIDO
     * ============================================================
     */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception
    ) {

        Map<String, Object> body = new HashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );

        body.put(
                "erro",
                exception.getMessage()
        );


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }


    /*
     * ============================================================
     * VALIDAÇÃO DOS DTOs
     *
     * Exemplo:
     *
     * @NotNull
     * @NotBlank
     * @Email
     * @Size
     * ============================================================
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> erros =
                new HashMap<>();


        for (
                FieldError fieldError :
                exception
                        .getBindingResult()
                        .getFieldErrors()
        ) {

            erros.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }


        Map<String, Object> body =
                new HashMap<>();


        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );

        body.put(
                "erro",
                "Existem campos inválidos na requisição."
        );

        body.put(
                "erros",
                erros
        );


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }


    /*
     * ============================================================
     * JSON INVÁLIDO / ENUM INVÁLIDO / DATA INVÁLIDA
     *
     * Exemplos:
     *
     * "status": "PAGO"
     *
     * quando o enum possui apenas:
     *
     * PENDENTE
     * PAGA
     * ATRASADA
     *
     *
     * Ou:
     *
     * "tipo": "FGTS"
     *
     * quando TipoObrigacao não possui FGTS.
     *
     *
     * Também trata datas incompatíveis com LocalDate.
     * ============================================================
     */

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception
    ) {

        Map<String, Object> body =
                new HashMap<>();


        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );

        body.put(
                "erro",
                "Um ou mais campos enviados possuem valores inválidos."
        );


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }


    /*
     * ============================================================
     * ACESSO NEGADO
     *
     * Exemplo:
     *
     * USUARIO tentando cadastrar uma obrigação.
     * ============================================================
     */

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException exception
    ) {

        Map<String, Object> body =
                new HashMap<>();


        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                HttpStatus.FORBIDDEN.value()
        );

        body.put(
                "erro",
                exception.getMessage() != null
                        ? exception.getMessage()
                        : "Você não possui permissão para realizar esta ação."
        );


        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(body);
    }
}