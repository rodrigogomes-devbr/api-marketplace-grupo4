package br.edu.fiap.marketplace.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.fiap.marketplace.dto.CarrinhoResponse;
import br.edu.fiap.marketplace.entity.StatusCarrinho;
import br.edu.fiap.marketplace.service.CarrinhoService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CarrinhoController.class)
@AutoConfigureMockMvc(addFilters = false)
class CarrinhoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarrinhoService carrinhoService;

    @Test
    void deveCriarCarrinhoValidoRetornando201ELocation() throws Exception {
        CarrinhoResponse resposta = new CarrinhoResponse(
                10L, 1L, "Mariana Costa", 3L, "Teclado mecânico",
                new BigDecimal("299.90"), 2, new BigDecimal("599.80"),
                StatusCarrinho.ABERTO, Instant.parse("2026-10-01T15:00:00Z"));
        when(carrinhoService.criar(any())).thenReturn(resposta);

        mockMvc.perform(post("/api/carrinhos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuarioId": 1, "produtoId": 3, "quantidade": 2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/carrinhos/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.total").value(599.8));
    }

    @Test
    void deveRetornar400QuandoQuantidadeForInvalida() throws Exception {
        mockMvc.perform(post("/api/carrinhos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuarioId": 1, "produtoId": 3, "quantidade": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.quantidade")
                        .value("Quantidade deve ser pelo menos 1."));

        verifyNoInteractions(carrinhoService);
    }
}