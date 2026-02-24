package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoEmAtendimento implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        System.out.println("Pagamento já confirmado.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.EM_ATENDIMENTO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        chamado.mudaEstado(ChamadoStatus.CONCLUIDO.getInstancia());
    }
}