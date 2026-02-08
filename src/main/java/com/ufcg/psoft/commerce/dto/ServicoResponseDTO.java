package com.ufcg.psoft.commerce.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.model.Plano;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.model.Urgencia;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "id obrigatorio")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("nome")
    @NotBlank(message = "nome obrigatorio")
    private String nome;

    @JsonProperty("tipo")
    @NotNull(message = "tipo obrigatorio")
    private TipoServico tipo;

    @JsonProperty("descricao")
    @NotBlank(message = "descricao obrigatoria")
    private String descricao;

    @JsonProperty("urgencia")
    @NotNull(message = "urgencia obrigatoria")
    private Urgencia urgencia;

    @JsonProperty("preco")
    @NotNull(message = "preco obrigatorio")
    private Double preco;

    @JsonProperty("disponivel")
    @NotNull(message = "disponibilidade obrigatoria")
    private Boolean disponivel;

    @JsonProperty("plano")
    @NotNull(message = "plano obrigatorio")
    private Plano plano;

    @JsonProperty("duracao")
    @NotNull(message = "duracao obrigatoria")
    private Double duracao;

    @JsonProperty("empresaId")
    @NotNull(message = "empresaId obrigatorio")
    private Long empresaId;

    public ServicoResponseDTO(Servico servico) {
        this.id = servico.getId();
        this.nome = servico.getNome();
        this.tipo = servico.getTipo();
        this.descricao = servico.getDescricao();
        this.urgencia = servico.getUrgencia();
        this.preco = servico.getPreco();
        this.disponivel = servico.isDisponivel();
        this.plano = servico.getPlano();
        this.duracao = servico.getDuracao();
        this.empresaId = servico.getEmpresa().getId();
    }
}