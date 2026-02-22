package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoAguardandoPagamento implements ChamadoEstado {
    
    @Override
    public void avancarEstado(Chamado chamado) {
        chamado.mudaEstado(new ChamadoEstadoRecebido());
    }

    @Override
    public String getNome() {
        return "AGUARDANDO_PAGAMENTO";
    }
}