package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoAguardandoPagamento implements ChamadoEstado {
    @Override
    public String getStatus() {
        return "AGUARDANDO_PAGAMENTO";
    }

    @Override
    public void confirmarPagamento(Chamado chamado) {
        chamado.setEstado(new ChamadoEstadoEmProcessamento());
    }
}