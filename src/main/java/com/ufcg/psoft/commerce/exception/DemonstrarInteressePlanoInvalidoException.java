package com.ufcg.psoft.commerce.exception;

public class DemonstrarInteressePlanoInvalidoException extends CommerceException{
   public DemonstrarInteressePlanoInvalidoException() {
        super("Não é possível demonstrar interesse à um serviço premium com seu plano atual.");
    } 
}
