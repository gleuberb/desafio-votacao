package com.desafio_votacao.api.model.pauta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PautaRequestDto {

    @NotBlank(message = "O título é obrigatório.")
    @Size(max = 255, message = "O título não pode ter mais de 255 caracteres.")
    private String titulo;

    @Size(max = 255, message = "A descrição não pode ter mais de 255 caracteres.")
    private String descricao;
}
