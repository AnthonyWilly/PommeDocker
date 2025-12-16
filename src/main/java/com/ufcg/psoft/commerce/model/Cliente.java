package com.ufcg.psoft.commerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("nome")
    @Column(nullable = false)
    private String nome;

    @JsonProperty("endereco")
    @Column(nullable = false)
    private String endereco;

    @JsonProperty("planoAtual")
    @Column(nullable = false)
    private String planoAtual;

    @JsonProperty("proxPlano")
    private String proxPlano;

    @JsonProperty("dataCobranca")
    @Column(nullable = false)
    private String dataCobranca;

    @JsonIgnore
    @Column(nullable = false)
    private String codigo;

    public void setPlanoPremium(String senha) {
        this.setPlano("Premium", senha);
    }
    public void setPlanoBasico(String senha) {
        this.setPlano("Basico", senha);
    }
    private void setPlano(String plano, String senha){
        if (!this.codigo.equals(senha)) {
            throw new IllegalArgumentException("Codigo de acesso invalido!");
        }
        this.proxPlano = plano;
    }
}
