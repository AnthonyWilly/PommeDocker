package com.ufcg.psoft.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponseDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nome")
    @NotBlank(message = "Nome obrigatorio")
    private String nome;

    @JsonProperty("cnpj")
    @NotBlank(message = "CNPJ obrigatorio")
    private String cnpj;
    
    public EmpresaResponseDTO(com.ufcg.psoft.commerce.model.Empresa empresa) {
        this.id = empresa.getId();
        this.nome = empresa.getNome();
        this.cnpj = empresa.getCnpj();
    }
}