package com.desafio_votacao.api.service;

import com.desafio_votacao.api.model.pauta.Pauta;
import com.desafio_votacao.api.model.voto.Voto;
import com.desafio_votacao.api.repository.VotoRepository;
import com.desafio_votacao.api.utils.CpfValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class VotoServiceTest {

    @Mock
    private PautaService pautaService;

    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private VotoService votoService;

    private Pauta pauta;
    private Voto voto;
    private String cpf;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pauta = new Pauta();
        pauta.setId(1L);
        pauta.setDataAbertura(LocalDateTime.now().minusMinutes(5));
        pauta.setDataFechamento(LocalDateTime.now().plusMinutes(5));

        cpf = "12345678909";

        voto = new Voto();
        voto.setPauta(pauta);
        voto.setCpf(cpf);
        voto.setVoto("SIM");
    }

    @Test
    void votar_DeveRegistrarVotoQuandoValido() {
        when(pautaService.buscarPautaPorId(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.existsByCpfAndPautaId(cpf, pauta.getId())).thenReturn(false);
        when(votoRepository.save(any(Voto.class))).thenReturn(voto);

        Voto result = votoService.votar(pauta.getId(), cpf, "SIM");

        assertNotNull(result);
        assertEquals("SIM", result.getVoto());
        verify(votoRepository, times(1)).save(any(Voto.class));
    }

    @Test
    void votar_DeveLancarErroQuandoPautaNaoEstiverAberta() {
        pauta.setDataFechamento(null);
        when(pautaService.buscarPautaPorId(1L)).thenReturn(Optional.of(pauta));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            votoService.votar(pauta.getId(), cpf, "SIM");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Pauta não foi aberta.", exception.getReason());
    }

    @Test
    void votar_DeveLancarErroQuandoPautaJaFechada() {
        pauta.setDataFechamento(LocalDateTime.now().minusMinutes(1));
        when(pautaService.buscarPautaPorId(1L)).thenReturn(Optional.of(pauta));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            votoService.votar(pauta.getId(), cpf, "SIM");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Pauta já foi fechada.", exception.getReason());
    }

    @Test
    void votar_DeveLancarErroQuandoCpfInvalido() {
        try (MockedStatic<CpfValidator> cpfValidatorMocked = Mockito.mockStatic(CpfValidator.class)) {
            cpfValidatorMocked.when(() -> CpfValidator.validarCPF(cpf)).thenReturn(false);
            when(pautaService.buscarPautaPorId(1L)).thenReturn(Optional.of(pauta));
            when(votoRepository.existsByCpfAndPautaId(cpf, pauta.getId())).thenReturn(false);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                votoService.votar(pauta.getId(), cpf, "SIM");
            });

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("CPF inválido.", exception.getReason());
        }
    }

    @Test
    void votar_DeveLancarErroQuandoVotoInvalido() {
        when(pautaService.buscarPautaPorId(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.existsByCpfAndPautaId(cpf, pauta.getId())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            votoService.votar(pauta.getId(), cpf, "INVALIDO");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Voto Inválido.", exception.getReason());
    }

    @Test
    void votar_DeveLancarErroQuandoCpfJaVotou() {
        when(pautaService.buscarPautaPorId(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.existsByCpfAndPautaId(cpf, pauta.getId())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            votoService.votar(pauta.getId(), cpf, "SIM");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Este CPF já votou nesta pauta.", exception.getReason());
    }

    @Test
    void contabilizarVotos_DeveContabilizarVotosCorretamente() {
        Voto votoSim = new Voto();
        votoSim.setPauta(pauta);
        votoSim.setCpf("12345678900");
        votoSim.setVoto("SIM");

        Voto votoNao = new Voto();
        votoNao.setPauta(pauta);
        votoNao.setCpf("12345678901");
        votoNao.setVoto("NAO");

        List<Voto> votos = Arrays.asList(votoSim, votoNao, votoSim);
        when(votoRepository.findByPautaId(pauta.getId())).thenReturn(votos);

        String result = votoService.contabilizarVotos(pauta.getId());

        assertNotNull(result);
        assertTrue(result.contains("Total de votos: 3"));
        assertTrue(result.contains("Votos no SIM: 2"));
        assertTrue(result.contains("Votos no NÃO: 1"));
        assertTrue(result.contains("Vencedor: 'SIM'"));
    }

    @Test
    void contabilizarVotos_DeveDeterminarVencedorCorretamenteQuandoVotoSimMaior() {
        Voto votoSim = new Voto();
        votoSim.setPauta(pauta);
        votoSim.setCpf("12345678900");
        votoSim.setVoto("SIM");

        Voto votoNao = new Voto();
        votoNao.setPauta(pauta);
        votoNao.setCpf("12345678901");
        votoNao.setVoto("NAO");

        List<Voto> votos = Arrays.asList(votoSim, votoNao, votoSim); // 2 votos SIM, 1 voto NAO
        when(votoRepository.findByPautaId(pauta.getId())).thenReturn(votos);

        String result = votoService.contabilizarVotos(pauta.getId());

        assertNotNull(result);
        assertTrue(result.contains("Vencedor: 'SIM'"));
    }

    @Test
    void contabilizarVotos_DeveDeterminarVencedorCorretamenteQuandoVotoNaoMaior() {
        Voto votoSim = new Voto();
        votoSim.setPauta(pauta);
        votoSim.setCpf("12345678900");
        votoSim.setVoto("SIM");

        Voto votoNao = new Voto();
        votoNao.setPauta(pauta);
        votoNao.setCpf("12345678901");
        votoNao.setVoto("NAO");

        List<Voto> votos = Arrays.asList(votoNao, votoNao, votoSim); // 1 voto SIM, 2 votos NAO
        when(votoRepository.findByPautaId(pauta.getId())).thenReturn(votos);

        String result = votoService.contabilizarVotos(pauta.getId());

        assertNotNull(result);
        assertTrue(result.contains("Vencedor: 'NAO'"));
    }

    @Test
    void votar_DeveLancarErroQuandoPautaNaoEncontrada() {
        when(pautaService.buscarPautaPorId(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            votoService.votar(pauta.getId(), cpf, "SIM");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Pauta não encontrada.", exception.getReason());
    }

    @Test
    void validaVoto_DeveRetornarTrueParaVotoValido() {
        assertTrue(votoService.validaVoto("SIM"));
        assertTrue(votoService.validaVoto("NAO"));
    }

    @Test
    void validaVoto_DeveRetornarFalseParaVotoInvalido() {
        assertFalse(votoService.validaVoto("INDEFINIDO"));
    }
}