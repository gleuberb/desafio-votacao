package com.desafio_votacao.api.service;

import com.desafio_votacao.api.model.pauta.Pauta;
import com.desafio_votacao.api.repository.PautaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private PautaService pautaService;

    private Pauta pauta;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pauta = new Pauta();
        pauta.setId(1L);
        pauta.setDataAbertura(null);
        pauta.setDataFechamento(null);
    }

    @Test
    void criarPauta() {
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);

        Pauta result = pautaService.criarPauta(pauta);

        assertNotNull(result);
        assertEquals(pauta.getId(), result.getId());
    }

    @Test
    void abrirSessaoPauta() {
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);

        Pauta result = pautaService.abrirSessaoPauta(1L);

        assertNotNull(result);
        assertNotNull(result.getDataAbertura());
        assertNotNull(result.getDataFechamento());
        assertTrue(result.getDataFechamento().isAfter(result.getDataAbertura()));
    }

    @Test
    void abrirSessaoPauta_DeveLancarErroQuandoPautaJaFoiAberta() {
        pauta.setDataAbertura(LocalDateTime.now());
        pauta.setDataFechamento(LocalDateTime.now().plusMinutes(1));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {pautaService.abrirSessaoPauta(1L);});

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void abrirSessaoPauta_DeveLancarErroQuandoPautaNaoForEncontrada() {
        when(pautaRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {pautaService.abrirSessaoPauta(1L);});

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void buscarPautaPorId_DeveRetornarPautaQuandoExistir() {
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        Optional<Pauta> result = pautaService.buscarPautaPorId(1L);

        assertTrue(result.isPresent());
        assertEquals(pauta.getId(), result.get().getId());
    }

    @Test
    void buscarPautaPorId_DeveRetornarVazioQuandoNaoExistir() {
        when(pautaRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Pauta> result = pautaService.buscarPautaPorId(1L);

        assertFalse(result.isPresent());
    }

}
