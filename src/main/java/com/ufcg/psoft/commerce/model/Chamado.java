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

    @Enumerated(EnumType.STRING)
    private StatusChamado status; 

    public void confirmarPagamento() {
        if (this.status == null) {
            this.status = StatusChamado.AGUARDANDO_PAGAMENTO; 
        }
        this.status.confirmarPagamento(this);
    }
    
    public void setStatus(StatusChamado novoStatus) {
        this.status = novoStatus;
    }
}