package com.ufcg.psoft.commerce.exception;

public class DemonstrarInteressePlanoInvalidoException extends CommerceException{
   public DemonstrarInteressePlanoInvalidoException() {
        super("Nao e possivel demonstrar interesse a um servico premium com seu plano atual.");
    } 
}
