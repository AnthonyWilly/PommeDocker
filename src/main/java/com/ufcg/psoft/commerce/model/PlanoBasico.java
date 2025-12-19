package com.ufcg.psoft.commerce.model;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component("Basico")
public class PlanoBasico implements Plano {

    private List<String> servicos;
    private BigDecimal preco;
    private int prioridade;

    public PlanoBasico(){
        this.servicos = new ArrayList<>();
        this.preco = BigDecimal.valueOf(29.90);
        this.prioridade = 6;
    }

    public String getPlano(){
        return "Basico";
    }

    public BigDecimal getValorDesconto(){
        return BigDecimal.valueOf(0);
    }
    
    public boolean addServico(String novoServico) {
        servicos.add(novoServico);
        return true;
    }

}