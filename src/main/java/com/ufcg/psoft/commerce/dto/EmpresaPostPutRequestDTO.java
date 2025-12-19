package com.ufcg.psoft.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaPostPutRequestDTO {
    @NotBlank(message = "Nome obrigatorio")
    private String nome;

    @NotBlank(message = "CNPJ obrigatorio")
    private String cnpj;

    @Pattern(regexp = "^\\d+$", message = "Codigo de acesso deve conter apenas digitos")
    @Size(min = 6, max = 6, message = "Codigo de acesso deve ter exatamente 6 digitos")
    private String codigoAcesso;

    @NotBlank(message = "Senha de administrador obrigatoria")
    private String senhaAdmin;
}