package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoCancelado implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        System.out.println("O chamado foi cancelado.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.CANCELADO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        System.out.println("Um chamado cancelado não pode avançar de estado.");
    }
}