package com.ufcg.psoft.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoPostPutRequestDTO {
    @NotNull(message = "Id do serviço é obrigatório")
    @JsonProperty("servicoId")
    private Long servicoId;

    @NotNull(message = "Id da empresa é obrigatório")
    @JsonProperty("empresaId")
    private Long empresaId;

    @JsonProperty("enderecoAtendimento")
    private String enderecoAtendimento;
}