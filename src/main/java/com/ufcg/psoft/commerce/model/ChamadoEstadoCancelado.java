package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoCancelado implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        throw new RuntimeException("O chamado foi cancelado.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.CANCELADO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        throw new RuntimeException("Um chamado cancelado não pode avançar de estado.");
    }

    @Override
    public void atribuirTecnico(Chamado chamado, Tecnico tecnico) {
        throw new RuntimeException("Não é possível atribuir um técnico neste status do chamado.");
    }
}