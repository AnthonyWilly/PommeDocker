package com.ufcg.psoft.commerce.service.notificacao;

import com.ufcg.psoft.commerce.model.Chamado;
import com.ufcg.psoft.commerce.model.ListenerChamado;
import com.ufcg.psoft.commerce.model.Tecnico;
import org.springframework.stereotype.Component;

@Component
public class EmpresaNotificacaoObserver implements ListenerChamado {

    @Override
    public void notificarConclusao(Chamado chamado) {
        System.out.println("Destinatário: Empresa ID " + chamado.getEmpresa().getId());
        System.out.println("Motivo: O chamado de ID " + chamado.getId() + " mudou o status para CONCLUÍDO.");
    }

    @Override
    public void notificar(Tecnico tecnico) {

    }
}