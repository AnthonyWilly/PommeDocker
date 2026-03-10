package com.ufcg.psoft.commerce.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
