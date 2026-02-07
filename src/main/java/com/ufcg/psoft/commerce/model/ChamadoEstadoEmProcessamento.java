package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoEmProcessamento implements ChamadoEstado {
    @Override
    public String getStatus() {
        return "EM_PROCESSAMENTO";
    }

    @Override
    public void confirmarPagamento(Chamado chamado) {
    }
}