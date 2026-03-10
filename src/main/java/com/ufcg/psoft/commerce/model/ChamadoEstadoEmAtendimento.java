package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoEmAtendimento implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        throw new RuntimeException("Pagamento já confirmado.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.EM_ATENDIMENTO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        chamado.setStatus(ChamadoStatus.AGUARDANDO_CONFIRMACAO.getNome());
        chamado.setEstado(new ChamadoEstadoAguardandoConfirmacao());
    }

    @Override
    public void atribuirTecnico(Chamado chamado, Tecnico tecnico) {
        throw new RuntimeException("Não é possível atribuir um técnico neste status do chamado.");
    }
}