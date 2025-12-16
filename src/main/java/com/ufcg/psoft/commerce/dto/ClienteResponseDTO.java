package com.ufcg.psoft.commerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.model.Cliente;
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
public class ClienteResponseDTO {

    @JsonProperty("id")
    @Id
    @NotBlank(message = "Id obrigatorio")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("nome")
    @NotBlank(message = "Nome obrigatorio")
    private String nome;

    @JsonProperty("endereco")
    @NotBlank(message = "Endereco obrigatorio")
    private String endereco;

    @JsonProperty("planoAtual")
    @NotBlank(message = "Plano obrigatorio")
    private String planoAtual;

    public ClienteResponseDTO(Cliente cliente) {
        this.id = cliente.getId();
        this.nome = cliente.getNome();
        this.endereco = cliente.getEndereco();
        this.planoAtual = cliente.getPlanoAtual();
    }
}

