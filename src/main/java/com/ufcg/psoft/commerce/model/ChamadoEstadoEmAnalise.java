    package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoEmAnalise implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        System.out.println("Pagamento já confirmado.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.EM_ANALISE.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        chamado.mudaEstado(ChamadoStatus.AGUARDANDO_TECNICO.getInstancia());
    }
}