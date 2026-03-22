package com.ufcg.psoft.commerce.model;

import com.ufcg.psoft.commerce.exception.CommerceException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class Pagamento {

    private final Map<String, PagamentoStrategy> estrategiasPorMetodo;

    public Pagamento(List<PagamentoStrategy> estrategias) {
        this.estrategiasPorMetodo = estrategias.stream()
                .collect(Collectors.toMap(
                        estrategia -> estrategia.getMetodo().toUpperCase(Locale.ROOT),
                        Function.identity()
                ));
    }

    public BigDecimal calcularValorFinal(String metodoPagamento, BigDecimal valorTotal) {
        if (metodoPagamento == null || metodoPagamento.isBlank()) {
            throw new CommerceException("Metodo de pagamento nao suportado");
        }

        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new CommerceException("Valor total invalido");
        }

        PagamentoStrategy estrategia = estrategiasPorMetodo.get(metodoPagamento.toUpperCase(Locale.ROOT));
        if (estrategia == null) {
            throw new CommerceException("Metodo de pagamento nao suportado");
        }

        return estrategia.aplicarDesconto(valorTotal);
    }
}
