package com.ufcg.psoft.commerce.model;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component("Premium")
public class PlanoPremium implements Plano {
    private List<String> servicos;
    private BigDecimal preco;
    private int prioridade;
    public PlanoPremium(){
        this.servicos = new ArrayList<>();
        this.preco = BigDecimal.valueOf(49.90);
        this.prioridade = 10;
    }
    public String getPlano(){
        return "Premium";
    }
    public BigDecimal getValorDesconto()
    {
        return BigDecimal.valueOf(0.35);
    }
    public boolean addServico(String novoServico) {
        servicos.add(novoServico);
        return true;
    }

}

