package com.ufcg.psoft.commerce.service.servico;

import java.util.List;

import com.ufcg.psoft.commerce.dto.ServicoFiltroDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;

public interface ServicoService {
    public List<ServicoResponseDTO> listarCatalogoServicoCliente(Long clienteId, ServicoFiltroDTO filtro);
}
