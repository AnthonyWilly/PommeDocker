package com.ufcg.psoft.commerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder(toBuilder = true)
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

    @JsonProperty("statusDisponibilidade")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusDisponibilidade statusDisponibilidade =
        StatusDisponibilidade.DESCANSO;

    @JsonProperty("dataUltimaMudancaDisponibilidade")
    @Column
    private LocalDateTime dataUltimaMudancaDisponibilidade;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tecnico_empresa_aprovacao",
        joinColumns = @JoinColumn(name = "tecnico_id"),
        inverseJoinColumns = @JoinColumn(name = "empresa_id")
    )
    @Builder.Default
    private List<Empresa> empresasAprovadoras = new ArrayList<>();

    public boolean isAprovado() {
        return !empresasAprovadoras.isEmpty();
    }
}
