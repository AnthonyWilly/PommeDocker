package com.ufcg.psoft.commerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tecnico {
    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("nome")
    @Column(nullable = false)
    private String nome;

    @JsonProperty("especialidade")
    @Column(nullable = false)
    private String especialidade;

    @JsonProperty("corVeiculo")
    @Column(nullable = false)
    private String corVeiculo;

    @JsonProperty("tipoVeiculo")
    @Column(nullable = false)
    private TipoVeiculo tipoVeiculo;

    @JsonProperty("placaVeiculo")
    @Column(nullable = false)
    private String placaVeiculo;

    @JsonIgnore
    @Column(nullable = false)
    private String acesso;


}

