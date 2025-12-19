package com.ufcg.psoft.commerce.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "empresas")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cnpj;
    private String codigoAcesso;
}