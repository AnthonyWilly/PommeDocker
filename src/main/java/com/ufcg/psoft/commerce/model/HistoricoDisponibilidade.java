package com.ufcg.psoft.commerce.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoDisponibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("tecnicoId")
    @Column(nullable = false)
    private Long tecnicoId;

    @JsonProperty("novoStatus")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusDisponibilidade novoStatus;

    @JsonProperty("dataHora")
    @Column(nullable = false)
    private LocalDateTime dataHora;
}
