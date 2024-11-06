package com.desafio_votacao.api.controller;

import com.desafio_votacao.api.model.pauta.Pauta;
import com.desafio_votacao.api.model.pauta.dto.PautaRequestDto;
import com.desafio_votacao.api.service.PautaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/pautas")
public class PautaController {

    private final PautaService pautaService;

    public PautaController(PautaService pautaService){
        this.pautaService = pautaService;
    }

    @PostMapping
    public Pauta criarPauta(@RequestBody PautaRequestDto dto) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(dto.getTitulo());
        pauta.setDescricao(dto.getDescricao());
        return pautaService.criarPauta(pauta);
    }

    @PostMapping("/abrirSessao/{pautaId}")
    public Pauta abrirSessaoPauta(@PathVariable Long pautaId) {
        return pautaService.abrirSessaoPauta(pautaId);
    }


    @GetMapping("/{pautaId}")
    public Pauta buscarPautaPorId(@PathVariable Long pautaId) {
        return pautaService.buscarPautaPorId(pautaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pauta não encontrada."));
    }
}