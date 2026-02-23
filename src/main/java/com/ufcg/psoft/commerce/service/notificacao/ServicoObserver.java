package com.ufcg.psoft.commerce.service.notificacao;

import com.ufcg.psoft.commerce.model.Servico;

public interface ServicoObserver {
    void notificar(Servico servico);
}