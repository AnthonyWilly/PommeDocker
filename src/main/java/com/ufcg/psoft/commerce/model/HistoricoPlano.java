package com.ufcg.psoft.commerce.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class HistoricoPlano {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("idCliente")
    @Column(nullable = false)
    private long idCliente;

    @JsonProperty("idPlanoAntigo")
    @Column(nullable = false)
    private String idPlanoAntigo;

    @JsonProperty("data")
    @Column(nullable = false)
    private LocalDate data;

}