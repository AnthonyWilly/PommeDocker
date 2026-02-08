package com.ufcg.psoft.commerce.model;

public enum StatusChamado implements ChamadoEstado {

    AGUARDANDO_PAGAMENTO {
        @Override
        public void confirmarPagamento(Chamado chamado) {
            chamado.setStatus(EM_PROCESSAMENTO);
        }

        @Override
        public String getStatus() {
            return "AGUARDANDO_PAGAMENTO";
        }
    },

    EM_PROCESSAMENTO {
        @Override
        public void confirmarPagamento(Chamado chamado) {
            // Lógica para confirmar pagamento
        }

        @Override
        public String getStatus() {
            return "EM_PROCESSAMENTO";
        }
    };
}