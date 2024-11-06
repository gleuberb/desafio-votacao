package com.desafio_votacao.api.controller;

import com.desafio_votacao.api.model.voto.Voto;
import com.desafio_votacao.api.service.PautaService;
import com.desafio_votacao.api.service.VotoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pautas/votos/{pautaId}")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService, PautaService pautaService){
        this.votoService = votoService;
    }

    @PostMapping
    public Voto votar(@PathVariable Long pautaId, @RequestParam String associadoId, @RequestParam String voto) {
        return votoService.votar(pautaId, associadoId, voto);
    }

    @GetMapping("/resultado")
    public String contabilizarVotos(@PathVariable Long pautaId) {
        return votoService.contabilizarVotos(pautaId);
    }
}
