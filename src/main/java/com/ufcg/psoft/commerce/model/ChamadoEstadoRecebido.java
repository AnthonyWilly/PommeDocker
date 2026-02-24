package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoRecebido implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        System.out.println("Pagamento já confirmado.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.RECEBIDO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        chamado.mudaEstado(ChamadoStatus.EM_ANALISE.getInstancia());
    }
}