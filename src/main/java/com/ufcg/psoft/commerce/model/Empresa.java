package com.ufcg.psoft.commerce.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "empresas")
public class Empresa {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("cnpj")
    @Column(nullable = false, unique = true)
    private String cnpj;

    @JsonProperty("nome")
    @Column(nullable = false)
    private String nome;

    @JsonProperty("codigoAcesso")
    @Column(nullable = false)
    private String codigoAcesso;
}
