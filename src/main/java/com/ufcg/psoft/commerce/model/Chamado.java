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

    private String status; 

    @Transient
    private ChamadoEstado estado;

    public Chamado() {
        this.estado = new ChamadoEstadoAguardandoPagamento();
        this.status = this.estado.getNome();
    }
    public void mudaEstado(ChamadoEstado novoEstado) {
        this.estado = novoEstado;
        this.status = novoEstado.getNome();
    }

    public void confirmarPagamento() {
        this.estado.confirmarPagamento(this);
    }

    @PostLoad
    private void carregarEstado() {
        if (this.status == null) {
            this.estado = new ChamadoEstadoAguardandoPagamento();
            return;
        }
        
        switch (this.status) {
            case "AGUARDANDO_PAGAMENTO":
                this.estado = new ChamadoEstadoAguardandoPagamento();
                break;
            case "EM_PROCESSAMENTO":
                this.estado = new ChamadoEstadoEmProcessamento();
                break;
            default:
                this.estado = new ChamadoEstadoAguardandoPagamento();
        }
    }
}