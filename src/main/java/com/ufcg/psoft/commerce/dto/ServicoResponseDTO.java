package com.ufcg.psoft.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.model.Urgencia;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicoResponseDTO {

    @JsonProperty("id")
    @Id
    @NotBlank(message = "Id obrigatorio")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("nome")
    @NotBlank(message = "Id obrigatorio")
    private String nome;

    @JsonProperty("tipo")
    @NotBlank(message = "Id obrigatorio")
    private TipoServico tipo;

    @JsonProperty("descricao")
    @NotBlank(message = "descricao obrigatoria")
    private String descricao;

    @JsonProperty("urgencia")
    @NotBlank(message = "urgencia obrigatoria")
    private Urgencia urgencia;

    @JsonProperty("preco")
    @NotBlank(message = "preco obrigatorio")
    private Double preco;

    @JsonProperty("disponivel")
    @NotBlank(message = "disponibilidade obrigatoria")
    private Boolean disponivel;

    @JsonProperty("idPlano")
    @NotBlank(message = "idPlano obrigatorio")
    private String idPlano;

    @JsonProperty("duracao")
    @NotBlank(message = "duracao obrigatoria")
    private Double duracao;

    @JsonProperty("empresaId")
    @NotBlank(message = "Id obrigatorio")
    private Long empresaId;

    public ServicoResponseDTO(Servico servico) {
        this.id = servico.getId();
        this.nome = servico.getNome();
        this.tipo = servico.getTipo();
        this.descricao = servico.getDescricao();
        this.urgencia = servico.getUrgencia();
        this.preco = servico.getPreco();
        this.disponivel = servico.isDisponivel();
        this.idPlano = servico.getIdPlano();
        this.duracao = servico.getDuracao();
        this.empresaId = servico.getEmpresa().getId();
    }

}