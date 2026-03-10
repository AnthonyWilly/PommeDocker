package com.ufcg.psoft.commerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
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

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tecnico_empresa_aprovacao",
        joinColumns = @JoinColumn(name = "tecnico_id"),
        inverseJoinColumns = @JoinColumn(name = "empresa_id")
    )
    @Builder.Default
    private List<Empresa> empresasAprovadoras = new ArrayList<>();

    @Builder.Default
    private StatusTecnico statusTecnico = StatusTecnico.ATIVO;

    public boolean isAprovado() {
        return !empresasAprovadoras.isEmpty();
    }
}

