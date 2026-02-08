package com.ufcg.psoft.commerce.dto;
import com.ufcg.psoft.commerce.model.Plano;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.model.Urgencia;
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
public class ServicoPostPutRequestDTO {

        @NotBlank(message = "nome obrigatorio")
        private String nome;

        @NotNull(message = "tipo obrigatorio")
        private TipoServico tipo;

        @NotBlank(message = "descricao obrigatoria")
        private String descricao;

        @NotNull(message = "urgencia obrigatoria")
        private Urgencia urgencia;

        @NotNull (message = "preço obrigatorio")
        private Double preco;

        @NotNull(message = "disponivel obrigatorio")
        private Boolean disponivel;

        @NotNull(message = "plano obrigatorio")
        private Plano plano;

        @NotNull(message = "duracao obrigatoria")
        private Double duracao;

    }
