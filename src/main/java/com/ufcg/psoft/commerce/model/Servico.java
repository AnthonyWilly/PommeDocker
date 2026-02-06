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
@Table(name = "servicos")
public class Servico {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("nome")
    @Column(nullable = false)
    private String nome;

    @JsonProperty("tipo")
    @Column(nullable = false)
    private TipoServico tipo;

    @JsonProperty("urgencia")
    @Column(nullable = false)
    private Urgencia urgencia;

    @JsonProperty("descricao")
    @Column(nullable = false)
    private String descricao;

    @JsonProperty("duracao")
    @Column(nullable = false)
    private double duracao;

    @JsonIgnore
    @Column(nullable = false)
    private boolean disponivel;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @JsonIgnore
    @Column(name = "idPlano")
    private String idPlano;

}