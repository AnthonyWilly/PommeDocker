package com.ufcg.psoft.commerce.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponseDTO {
    private Long id;
    private String nome;
    private String cnpj;
}