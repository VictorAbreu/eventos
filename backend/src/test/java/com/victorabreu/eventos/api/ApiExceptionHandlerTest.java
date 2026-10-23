package com.victorabreu.eventos.api;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ApiExceptionHandlerTest {
    @ParameterizedTest
    @CsvSource({
            "404, Recurso não encontrado",
            "405, Método não permitido",
            "415, Formato não suportado",
            "406, Formato de resposta não disponível",
            "502, Não foi possível atender à requisição"
    })
    void preservaStatusHttpETraduzMensagens(int status, String titulo) {
        var detail = new ApiExceptionHandler().unexpected(new ResponseStatusException(HttpStatus.valueOf(status), "detalhe interno"));
        assertThat(detail.getStatus()).isEqualTo(status);
        assertThat(detail.getTitle()).isEqualTo(titulo);
        assertThat(detail.getDetail()).doesNotContain("detalhe interno");
    }
    @Test void erroInesperadoNaoExpoeDetalhesInternos() {
        var detail = new ApiExceptionHandler().unexpected(new IllegalStateException("segredo do banco"));
        assertThat(detail.getStatus()).isEqualTo(500);
        assertThat(detail.getDetail()).doesNotContain("segredo").contains("Tente novamente");
    }
}
