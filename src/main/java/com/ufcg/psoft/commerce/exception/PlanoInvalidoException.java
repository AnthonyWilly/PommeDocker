package com.ufcg.psoft.commerce.exception;

public class PlanoInvalidoException extends CommerceException {
    public PlanoInvalidoException() {
        super("O plano do cliente não permite acesso a este serviço.");
    }
}