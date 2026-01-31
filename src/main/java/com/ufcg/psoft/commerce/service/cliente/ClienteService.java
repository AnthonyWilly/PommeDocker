package com.ufcg.psoft.commerce.service.cliente;

import com.ufcg.psoft.commerce.dto.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ClienteResponseDTO;
import com.ufcg.psoft.commerce.dto.PagamentoRequestDTO;
import com.ufcg.psoft.commerce.dto.PagamentoResponseDTO;
import com.ufcg.psoft.commerce.model.Cliente;

import java.util.List;

public interface ClienteService {

    ClienteResponseDTO alterar(Long id, String codigoAcesso, ClientePostPutRequestDTO clientePostPutRequestDTO);

    List<ClienteResponseDTO> listar();

    ClienteResponseDTO recuperar(Long id);

    ClienteResponseDTO criar(ClientePostPutRequestDTO clientePostPutRequestDTO);

    void remover(Long id, String codigoAcesso);

    List<ClienteResponseDTO> listarPorNome(String nome);

    ClienteResponseDTO setPlanoPremium(long id, String codigoAcesso);

    ClienteResponseDTO setPlanoBasico(long id, String codigoAcesso);

    PagamentoResponseDTO confirmarPagamento(Long id, String codigoAcesso, PagamentoRequestDTO pagamentoRequestDTO);

}
