package com.ufcg.psoft.commerce.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    @Column(name = "plano")
    private Plano plano;

    @JsonProperty("preco")
    @Column(nullable = false)
    private double preco;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "servico_interessados",
            joinColumns = @JoinColumn(name = "servico_id"),
            inverseJoinColumns = @JoinColumn(name = "cliente_id")
    )
    @OrderBy("planoAtual DESC")
    @Builder.Default
    private List<Cliente> interessados = new ArrayList<>();
}