package com.ufcg.psoft.commerce.model;

public interface ListenerChamado {
    void notificar(Tecnico tecnico);
    void notificarFaltaDeTecnicos();
}
