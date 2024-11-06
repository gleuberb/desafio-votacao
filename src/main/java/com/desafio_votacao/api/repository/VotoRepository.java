package com.desafio_votacao.api.repository;

import com.desafio_votacao.api.model.voto.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VotoRepository extends JpaRepository<Voto, Long> {
    List<Voto> findByPautaId(Long pautaId);
    boolean existsByCpfAndPautaId(String cpf, Long pautaId);
}
