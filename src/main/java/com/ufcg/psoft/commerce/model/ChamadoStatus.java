package com.ufcg.psoft.commerce.model;

import java.util.function.Supplier;
import java.util.Arrays;

public enum ChamadoStatus {

    AGUARDANDO_PAGAMENTO("AGUARDANDO_PAGAMENTO", ChamadoEstadoAguardandoPagamento::new),
    EM_PROCESSAMENTO("EM_PROCESSAMENTO", ChamadoEstadoRecebido::new),
    RECEBIDO("CHAMADO_RECEBIDO", ChamadoEstadoRecebido::new),
    EM_ANALISE("EM_ANALISE", ChamadoEstadoEmAnalise::new),
    AGUARDANDO_TECNICO("AGUARDANDO_TECNICO", ChamadoEstadoAguardandoTecnico::new),
    EM_ATENDIMENTO("EM_ATENDIMENTO", ChamadoEstadoEmAtendimento::new),
    CONCLUIDO("CONCLUIDO", ChamadoEstadoConcluido::new);
    private final String nome;
    private final Supplier<ChamadoEstado> factory;

    ChamadoStatus(String nome, Supplier<ChamadoEstado> factory) {
        this.nome = nome;
        this.factory = factory;
    }

    public String getNome() {
        return nome;
    }

    public ChamadoEstado getInstancia() {
        return factory.get();
    }

    public static ChamadoEstado obterEstado(String status) {
        if (status == null) {
            return new ChamadoEstadoAguardandoPagamento();
        }

        return Arrays.stream(ChamadoStatus.values())
                .filter(s -> s.getNome().equals(status))
                .findFirst()
                .map(ChamadoStatus::getInstancia)
                .orElse(new ChamadoEstadoAguardandoPagamento());
    }
}