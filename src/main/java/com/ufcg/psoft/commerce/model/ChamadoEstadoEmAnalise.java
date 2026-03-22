    package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoEmAnalise implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        throw new RuntimeException("Pagamento já confirmado.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.EM_ANALISE.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        chamado.mudaEstado(ChamadoStatus.AGUARDANDO_TECNICO.getInstancia());
    }

    @Override
    public void atribuirTecnico(Chamado chamado, Tecnico tecnico) {
        throw new RuntimeException("Não é possível atribuir um técnico neste status do chamado.");
    }
}