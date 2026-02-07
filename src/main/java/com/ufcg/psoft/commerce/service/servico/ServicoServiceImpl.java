package com.ufcg.psoft.commerce.service.servico;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.commerce.dto.ServicoFiltroDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import com.ufcg.psoft.commerce.exception.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;

@Service
public class ServicoServiceImpl implements ServicoService {

    @Autowired
    ServicoRepository servicoRepository;

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public List<ServicoResponseDTO> listarCatalogoServicoCliente(Long clienteId, ServicoFiltroDTO filtro) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNaoExisteException());

        List<String> planos = new ArrayList<String>();
        planos.add("BASICO");

        if("PREMIUM".equals(cliente.getPlanoAtual().toUpperCase()))
            planos.add("PREMIUM");

        List<Servico> servicos = servicoRepository.findAllComFiltros(
            filtro.getTipo(),
            filtro.getEmpresaId(),
            filtro.getUrgencia(),
            filtro.getPrecoMin(),
            filtro.getPrecoMax(),
            planos
        );

        return servicos.stream()
                .map(ServicoResponseDTO::new)
                .collect(Collectors.toList());
    }

}