package com.ufcg.psoft.commerce.exception;

public class EmpresaNaoExisteException extends CommerceException {
    public EmpresaNaoExisteException() {
        super("A empresa consultada nao existe!");
    }
}
