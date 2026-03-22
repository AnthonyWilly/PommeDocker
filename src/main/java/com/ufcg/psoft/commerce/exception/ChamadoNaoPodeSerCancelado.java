package com.ufcg.psoft.commerce.exception;

public class ChamadoNaoPodeSerCancelado extends RuntimeException {
    public ChamadoNaoPodeSerCancelado() {
        super("O Chamado Não Pode Ser Cancelado no Estado Atual!");
    }
}
