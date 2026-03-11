package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoAguardandoConfirmacao implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        throw new RuntimeException("O chamado já foi pago e está aguardando confirmação de conclusão.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.AGUARDANDO_CONFIRMACAO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        throw new RuntimeException("O chamado está aguardando confirmação do cliente. Apenas o cliente pode concluí-lo.");
    }

    @Override
    public void atribuirTecnico(Chamado chamado, Tecnico tecnico) {
        throw new RuntimeException("Não é possível atribuir um técnico neste status do chamado.");
    }

    public void confirmarConclusao(Chamado chamado) {
        chamado.setStatus(ChamadoStatus.CONCLUIDO.getNome());
        chamado.setEstado(new ChamadoEstadoConcluido());
    }
}
