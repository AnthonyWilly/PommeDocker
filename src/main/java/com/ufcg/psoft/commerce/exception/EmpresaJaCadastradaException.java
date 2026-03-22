package com.ufcg.psoft.commerce.exception;

public class EmpresaJaCadastradaException extends CommerceException {
    public EmpresaJaCadastradaException() {
        super("Empresa ja cadastrada!");
    }
}