package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoRecebido implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        throw new RuntimeException("Pagamento já confirmado.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.RECEBIDO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        chamado.mudaEstado(ChamadoStatus.EM_ANALISE.getInstancia());
    }

    @Override
    public void atribuirTecnico(Chamado chamado, Tecnico tecnico) {
        throw new RuntimeException("Não é possível atribuir um técnico neste status do chamado.");
    }
}