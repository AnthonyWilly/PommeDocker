package com.ufcg.psoft.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public class ClientePlanoPostPutRequestDTO {
   
    @JsonProperty("codigo")
    @NotBlank(message = "Acesso obrigatorio")
    private String acesso;

    @JsonProperty("plano")
    @NotBlank(message = "Plano obrigatorio")
    private String plano;

}
