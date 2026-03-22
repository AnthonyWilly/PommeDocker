package com.ufcg.psoft.commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoResponseDTO {
    private Long id;
    private Long clienteId;
    private Long empresaId;
    private Long servicoId;
    private String status;
    private String enderecoAtendimento;
}