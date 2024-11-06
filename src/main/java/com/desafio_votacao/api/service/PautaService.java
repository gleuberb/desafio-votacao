package com.desafio_votacao.api.service;

import com.desafio_votacao.api.model.pauta.Pauta;
import com.desafio_votacao.api.repository.PautaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PautaService {

    private final PautaRepository pautaRepository;

    public PautaService(PautaRepository pautaRepository) {
        this.pautaRepository = pautaRepository;
    }

    public Pauta criarPauta(Pauta pauta) {
        return pautaRepository.save(pauta);
    }

    public Pauta abrirSessaoPauta(Long pautaId) {

        Pauta pauta = buscarPautaPorId(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pauta não encontrada."));

        if (pauta.getDataFechamento() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pauta já foi aberta.");
        }

        pauta.setDataAbertura(LocalDateTime.now());
        pauta.setDataFechamento(pauta.getDataAbertura().plusMinutes(1));
        return pautaRepository.save(pauta);
    }

    public Optional<Pauta> buscarPautaPorId(Long id) {
        return pautaRepository.findById(id);
    }
}
