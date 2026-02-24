package com.ufcg.psoft.commerce.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "servico_id")
    private Servico servico;

    private String enderecoAtendimento;
    
    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Tecnico tecnico;
    private String status; 

    @Transient
    private ChamadoEstado estado;

    @Transient
    private ListenerChamado listenerChamado;

    public Chamado() {
        this.estado = ChamadoStatus.AGUARDANDO_PAGAMENTO.getInstancia();
        this.status = ChamadoStatus.AGUARDANDO_PAGAMENTO.getNome();
    }

    public ChamadoEstado getEstado() {
        if (this.estado == null) {
            this.estado = ChamadoStatus.obterEstado(this.status);
        }
        return this.estado;
    }

    public void mudaEstado(ChamadoEstado novoEstado) {
        if ("AGUARDANDO_TECNICO".equals(this.status) && listenerChamado != null) {
            notificarObservers();
        }
        this.estado = novoEstado;
        this.status = novoEstado.getNome();
    }

    public void confirmarPagamento() {
        this.getEstado().confirmarPagamento(this);
    }

    @PostLoad
    private void carregarEstado() {
        this.estado = ChamadoStatus.obterEstado(this.status);
    }
    public void adicionarObserver(ListenerChamado observer){
        this.listenerChamado = observer;
    }
    private void notificarObservers(){
        listenerChamado.notificar(this.tecnico);
    }
}