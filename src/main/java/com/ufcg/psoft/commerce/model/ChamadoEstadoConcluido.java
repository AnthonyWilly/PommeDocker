package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoConcluido implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        throw new RuntimeException("O chamado já foi concluído.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.CONCLUIDO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        throw new RuntimeException("O chamado já está concluído e não pode avançar.");
    }

    @Override
    public void atribuirTecnico(Chamado chamado, Tecnico tecnico) {
        throw new RuntimeException("Não é possível atribuir um técnico neste status do chamado.");
    }
}