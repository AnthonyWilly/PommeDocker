package com.ufcg.psoft.commerce.service.cliente;

import com.ufcg.psoft.commerce.dto.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ClienteResponseDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;

import java.util.List;

public interface ClienteService {

    ClienteResponseDTO criar(ClientePostPutRequestDTO dto);
    ClienteResponseDTO recuperar(Long id);
    List<ClienteResponseDTO> listar();
    List<ClienteResponseDTO> listarPorNome(String nome);
    ClienteResponseDTO alterar(Long id, String codigo, ClientePostPutRequestDTO dto);
    void remover(Long id, String codigo);
    ClienteResponseDTO setPlanoPremium(Long id, String codigo);
    ClienteResponseDTO setPlanoBasico(Long id, String codigo);
}
