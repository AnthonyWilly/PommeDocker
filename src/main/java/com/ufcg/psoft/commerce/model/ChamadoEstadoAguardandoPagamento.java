package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoAguardandoPagamento implements ChamadoEstado {
    
    @Override
    public void confirmarPagamento(Chamado chamado) {
        chamado.mudaEstado(new ChamadoEstadoEmProcessamento());
    }

    @Override
    public String getNome() {
        return "AGUARDANDO_PAGAMENTO";
    }
}