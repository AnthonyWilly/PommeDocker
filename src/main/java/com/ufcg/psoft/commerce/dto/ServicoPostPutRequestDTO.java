package com.ufcg.psoft.commerce.dto;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.model.Urgencia;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicoPostPutRequestDTO {

        @NotBlank(message = "nome obrigatorio")
        private String nome;

        @NotBlank(message = "tipo obrigatorio")
        private TipoServico tipo;

        @NotBlank(message = "descricao obrigatoria")
        private String descricao;

        @NotBlank(message = "urgencia obrigatoria")
        private Urgencia urgencia;

        @NotBlank
        @NotBlank(message = "preço obrigatorio")
        private Double preco;

        @NotBlank
        @NotBlank(message = "disponivel obrigatorio")
        private Boolean disponivel;

        @NotBlank
        @NotBlank(message = "idPlano obrigatorio")
        private String idPlano;

        @NotBlank
        @NotBlank(message = "duracao obrigatoria")
        private Double duracao;

    }
