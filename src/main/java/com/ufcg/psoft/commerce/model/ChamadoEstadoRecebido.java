package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoRecebido implements ChamadoEstado {
    
    @Override
    public void avancarEstado(Chamado chamado) {
        System.out.println("O pagamento já foi confirmado anteriormente.");
    }

    @Override
    public String getNome() {
<<<<<<<< HEAD:src/main/java/com/ufcg/psoft/commerce/model/ChamadoEstadoRecebido.java
        return "CHAMADO_RECEBIDO";
========
        return ChamadoStatus.EM_PROCESSAMENTO.getNome();
    }

    @Override
    public void avancar(Chamado chamado) {
        chamado.mudaEstado(ChamadoStatus.RECEBIDO.getInstancia());
>>>>>>>> origin/feature/notificacao-de-inicio-de-atendimento:src/main/java/com/ufcg/psoft/commerce/model/ChamadoEstadoEmProcessamento.java
    }
}