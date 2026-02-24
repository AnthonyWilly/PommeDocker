package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoAguardandoTecnico implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        throw new RuntimeException("Pagamento já confirmado.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.AGUARDANDO_TECNICO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        throw new RuntimeException("Aguardando alocação do técnico.");
    }

    @Override
    public void atribuirTecnico(Chamado chamado, Tecnico tecnico) {
        chamado.setTecnico(tecnico);
        chamado.mudaEstado(ChamadoStatus.EM_ATENDIMENTO.getInstancia()); 
    }

}