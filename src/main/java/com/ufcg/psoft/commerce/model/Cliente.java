package com.ufcg.psoft.commerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.commerce.service.notificacao.ServicoObserver;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clientes")
public class Cliente implements ServicoObserver {

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
    private Plano planoAtual;

    @JsonProperty("proxPlano")
    private Plano proxPlano;

    @JsonProperty("dataCobranca")
    @Column(nullable = false)
    private LocalDate dataCobranca;

    @JsonIgnore
    @Column(nullable = false, length = 6)
    private String codigo;

    public void setPlanoPremium(String senha) {
        this.setPlano(Plano.PREMIUM, senha);
    }
    public void setPlanoBasico(String senha) {
        this.setPlano(Plano.BASICO, senha);
    }
    private void setPlano(Plano plano, String senha){
        if (!this.codigo.equals(senha)) {
            throw new IllegalArgumentException("Codigo de acesso invalido!");
        }
        this.proxPlano = plano;
    }

    @Override
    public void notificar(Servico servico) {
        System.out.println(
                "[NOTIFICAÇÃO] Cliente '" + this.nome +
                "' (id=" + this.id + "): " +
                "O serviço '" + servico.getNome() +
                "' (id=" + servico.getId() + ") que você demonstrou interesse " +
                "voltou a estar disponível!"
        );
    }
}