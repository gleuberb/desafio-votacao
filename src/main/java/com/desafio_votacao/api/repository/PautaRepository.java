package com.desafio_votacao.api.repository;

import com.desafio_votacao.api.model.pauta.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PautaRepository extends JpaRepository<Pauta, Long> {
}
