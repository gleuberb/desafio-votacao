package com.desafio_votacao.api.service;

import com.desafio_votacao.api.model.pauta.Pauta;
import com.desafio_votacao.api.model.voto.Voto;
import com.desafio_votacao.api.repository.VotoRepository;
import com.desafio_votacao.api.utils.CpfValidator;
import com.desafio_votacao.api.utils.enumerators.EVoto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VotoService {

    private final PautaService pautaService;

    private final VotoRepository votoRepository;

    public VotoService(PautaService pautaService, VotoRepository votoRepository) {
        this.pautaService = pautaService;
        this.votoRepository = votoRepository;
    }

    public Voto votar(Long pautaId, String cpf, String voto) {

        Pauta pauta = pautaService.buscarPautaPorId(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pauta não encontrada."));

        if (pauta.getDataFechamento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pauta não foi aberta.");
        }

        if (LocalDateTime.now().isAfter(pauta.getDataFechamento())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pauta já foi fechada.");
        }

        if (!CpfValidator.validarCPF(cpf)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF inválido.");
        }

        if (!validaVoto(voto)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voto Inválido.");
        }

        boolean existsVoto = votoRepository.existsByCpfAndPautaId(cpf, pautaId);

        if (existsVoto) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este CPF já votou nesta pauta.");
        }

        Voto novoVoto = new Voto();
        novoVoto.setPauta(pauta);
        novoVoto.setCpf(cpf);
        novoVoto.setVoto(voto.toUpperCase());
        return votoRepository.save(novoVoto);
    }

    public String contabilizarVotos(Long pautaId) {

        List<Voto> votos = votoRepository.findByPautaId(pautaId);

        Map<String, Long> votosContabilizados = votos.stream()
                .collect(Collectors.groupingBy(Voto::getVoto, Collectors.counting()));

        long totalVotos = votos.size();
        long votosSim = votosContabilizados.getOrDefault("SIM", 0L);
        long votosNao = votosContabilizados.getOrDefault("NAO", 0L);

        String vencedor = votosSim > votosNao ? "SIM" : "NAO";

        return "VotoResultado { " +
                "Total de votos: " + totalVotos +
                ", Votos no SIM: " + votosSim +
                ", Votos no NÃO: " + votosNao +
                ", Vencedor: '" + vencedor + '\'' +
                " }";
    }

    public boolean validaVoto(String voto) {
        try {
            EVoto.valueOf(voto.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
