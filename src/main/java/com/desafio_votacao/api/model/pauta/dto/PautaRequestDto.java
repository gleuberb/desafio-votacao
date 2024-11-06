package com.desafio_votacao.api.model.pauta.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PautaRequestDto {

    @NotBlank(message = "O título é obrigatório.")
    private String titulo;

    private String descricao;
}
