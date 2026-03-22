package com.ufcg.psoft.commerce.exception;

public class TecnicoNaoExisteException extends CommerceException {
    public TecnicoNaoExisteException() {
        super("O tecnico consultado nao existe!");
    }
}
