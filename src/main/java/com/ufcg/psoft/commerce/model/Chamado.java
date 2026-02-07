package com.ufcg.psoft.commerce.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
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

    private String status;

    @Transient
    private ChamadoEstado estado;

    @PostLoad
    private void carregarEstado() {
        if ("AGUARDANDO_PAGAMENTO".equals(this.status)) {
            this.estado = new ChamadoEstadoAguardandoPagamento();
        } else if ("EM_PROCESSAMENTO".equals(this.status)) {
            this.estado = new ChamadoEstadoEmProcessamento();
        }
    }

    @PrePersist
    @PreUpdate
    private void atualizarStatus() {
        if (this.estado != null) {
            this.status = this.estado.getStatus();
        }
    }

    public void confirmarPagamento() {
        if (this.estado == null) {
            this.carregarEstado();
        }
        this.estado.confirmarPagamento(this);
    }
    
    public void setEstado(ChamadoEstado novoEstado) {
        this.estado = novoEstado;
        this.status = novoEstado.getStatus();
    }
}