package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoConcluido implements ChamadoEstado {

    @Override
    public void confirmarPagamento(Chamado chamado) {
        System.out.println("O chamado já foi concluído.");
    }

    @Override
    public String getNome() {
        return ChamadoStatus.CONCLUIDO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        System.out.println("O chamado já está concluído e não pode avançar.");
    }
}