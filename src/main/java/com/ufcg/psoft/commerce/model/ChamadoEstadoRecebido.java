package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoRecebido implements ChamadoEstado {
    
    @Override
    public void avancarEstado(Chamado chamado) {
        System.out.println("O pagamento já foi confirmado anteriormente.");
    }

    @Override
    public String getNome() {
        return "CHAMADO_RECEBIDO";
    }
}