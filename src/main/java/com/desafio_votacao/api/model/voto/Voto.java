package com.desafio_votacao.api.model.voto;

import com.desafio_votacao.api.model.pauta.Pauta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Voto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Pauta pauta;

    private String cpf;

    private String voto;

    public Voto(Pauta pauta, String cpf, String voto) {
        this.pauta = pauta;
        this.cpf = cpf;
        this.voto = voto;
    }
}
