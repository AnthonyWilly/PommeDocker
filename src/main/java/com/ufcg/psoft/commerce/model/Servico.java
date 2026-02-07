package com.ufcg.psoft.commerce.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;
    private String nivelUrgencia;
    private String duracaoEstimada;
    private Double valor;
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
}