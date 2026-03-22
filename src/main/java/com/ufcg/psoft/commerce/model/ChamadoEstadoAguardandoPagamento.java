package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoAguardandoPagamento implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        chamado.mudaEstado(ChamadoStatus.EM_PROCESSAMENTO.getInstancia());
    }

    @Override
    public String getNome() {
        return ChamadoStatus.AGUARDANDO_PAGAMENTO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        throw new RuntimeException("Não é possível avançar. Aguardando confirmação de pagamento.");
    }

    public void atribuirTecnico(Chamado chamado, Tecnico tecnico) {
        throw new RuntimeException("Não é possível atribuir um técnico neste status do chamado.");
    }
}