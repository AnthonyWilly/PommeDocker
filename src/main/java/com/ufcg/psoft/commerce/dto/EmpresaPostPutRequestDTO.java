package com.ufcg.psoft.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaPostPutRequestDTO {

    @JsonProperty("nome")
    @NotBlank(message = "Nome obrigatorio")
    private String nome;

    @JsonProperty("cnpj")
    @NotBlank(message = "CNPJ obrigatorio")
    private String cnpj;

    @JsonProperty("codigoAcesso")
    @NotBlank(message = "Codigo de acesso obrigatorio")
    @Size(min = 6, max = 6, message = "Codigo de acesso deve ter exatamente 6 digitos")
    @Pattern(regexp = "\\d{6}", message = "Codigo de acesso deve conter apenas digitos")
    private String codigoAcesso;

    @JsonProperty("senhaAdmin")
    @NotBlank(message = "Senha de administrador obrigatoria")
    private String senhaAdmin;
}
