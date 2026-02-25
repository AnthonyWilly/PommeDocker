package com.ufcg.psoft.commerce.service.servico;
import java.util.List;

import com.ufcg.psoft.commerce.dto.ServicoFiltroDTO;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import com.ufcg.psoft.commerce.service.notificacao.ServicoObserver;
import java.util.List;

public interface ServicoService {

    ServicoResponseDTO alterar(Long empresaId, Long id, String codigoAcesso, ServicoPostPutRequestDTO servicoPostPutRequestDTO);

    List<ServicoResponseDTO> listar(Long idEmpresa);

    ServicoResponseDTO recuperar(Long idEmpresa, Long id);

    ServicoResponseDTO criar(Long idEmpresa, String codigoAcesso, ServicoPostPutRequestDTO servicoPostPutRequestDTO);

    void remover(Long idEmpresa, Long id, String codigoAcesso);

    List<ServicoResponseDTO> listarCatalogoServicoCliente(Long clienteId, ServicoFiltroDTO filtro);

    ServicoResponseDTO alterarDisponibilidade(Long empresaId, Long servicoId, String codigoAcesso, boolean disponivel);

    void registrarInteresse(Long clienteId, Long servicoId);

    void adicionarObservador(ServicoObserver observer);

}


