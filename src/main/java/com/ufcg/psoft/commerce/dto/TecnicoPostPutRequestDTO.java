package com.ufcg.psoft.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.model.TipoVeiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TecnicoPostPutRequestDTO {

    @JsonProperty("nome")
    @NotBlank(message = "Nome obrigatorio")
    private String nome;

    @JsonProperty("especialidade")
    @NotBlank(message = "Especialidade obrigatoria")
    private String especialidade;

    @JsonProperty("corVeiculo")
    @NotBlank(message = "Cor do Veículo obrigatoria")
    private String corVeiculo;

    @JsonProperty("tipoVeiculo")
    @NotBlank(message = "Tipo do Veiculo obrigatorio")
    private TipoVeiculo tipoVeiculo;

    @JsonProperty("placaVeiculo")
    @NotBlank(message = "Placa do Veiculo obrigatoria")
    private String placaVeiculo;

    @JsonProperty("acesso")
    @NotNull(message = "Codigo de acesso obrigatorio")
    @Pattern(regexp = "^\\d{6}$", message = "Codigo de acesso deve ter exatamente 6 digitos numericos")
    private String acesso;

    public TecnicoPostPutRequestDTO(Tecnico tecnico) {
        this.acesso = tecnico.getAcesso();
        this.nome = tecnico.getNome();
        this.corVeiculo = tecnico.getCorVeiculo();
        this.especialidade = tecnico.getEspecialidade();
        this.placaVeiculo = tecnico.getPlacaVeiculo();
        this.tipoVeiculo = tecnico.getTipoVeiculo();
    }
}
