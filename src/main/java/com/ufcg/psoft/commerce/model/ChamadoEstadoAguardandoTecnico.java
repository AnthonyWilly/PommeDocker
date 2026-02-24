package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoAguardandoTecnico implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        System.out.println("Pagamento já confirmado.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.AGUARDANDO_TECNICO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        chamado.mudaEstado(ChamadoStatus.EM_ATENDIMENTO.getInstancia());
    }
}