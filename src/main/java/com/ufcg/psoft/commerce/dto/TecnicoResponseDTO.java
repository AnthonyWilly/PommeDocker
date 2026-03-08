package com.ufcg.psoft.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.model.StatusDisponibilidade;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.model.TipoVeiculo;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class TecnicoResponseDTO {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("nome")
    @NotBlank(message = "Nome obrigatorio")
    private String nome;

    @JsonProperty("especialidade")
    @NotBlank(message = "Especialidade obrigatoria")
    @Column(nullable = false)
    private String especialidade;

    @JsonProperty("corVeiculo")
    @NotBlank(message = "Cor do Veículo obrigatoria")
    @Column(nullable = false)
    private String corVeiculo;

    @JsonProperty("tipoVeiculo")
    @NotBlank(message = "Tipo do Veiculo obrigatorio")
    @Column(nullable = false)
    private TipoVeiculo tipoVeiculo;

    @JsonProperty("placaVeiculo")
    @NotBlank(message = "Placa do Veiculo obrigatoria")
    @Column(nullable = false)
    private String placaVeiculo;

    @JsonProperty("statusDisponibilidade")
    private StatusDisponibilidade statusDisponibilidade;

    public TecnicoResponseDTO(Tecnico tecnico) {
        this.id = tecnico.getId();
        this.nome = tecnico.getNome();
        this.corVeiculo = tecnico.getCorVeiculo();
        this.especialidade = tecnico.getEspecialidade();
        this.placaVeiculo = tecnico.getPlacaVeiculo();
        this.tipoVeiculo = tecnico.getTipoVeiculo();
        this.statusDisponibilidade = tecnico.getStatusDisponibilidade();
    }
}
