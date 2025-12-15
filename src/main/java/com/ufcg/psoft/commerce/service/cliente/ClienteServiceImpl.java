package com.ufcg.psoft.commerce.service.cliente;

import com.ufcg.psoft.commerce.dto.ClientePlanoPostPutRequestDTO;
import com.ufcg.psoft.commerce.exception.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.dto.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ClienteResponseDTO;
import com.ufcg.psoft.commerce.repository.HistoricoRepository;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    ClienteRepository clienteRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    private PlanoBasico planoBasico;
    @Autowired
    private PlanoPremium planoPremium;
    @Autowired
    private Map<String, Plano> estrategias = new HashMap<>();
    @Autowired
    private HistoricoRepository  historicoRepository;

    @PostConstruct
    public void inicializarMapaDePlanos() {
        estrategias.put("Basico", planoBasico);
        estrategias.put("Premium", planoPremium);
    }

    @Override
    public ClienteResponseDTO alterar(Long id, String codigoAcesso, ClientePostPutRequestDTO clientePostPutRequestDTO) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(ClienteNaoExisteException::new);
        if (!cliente.getCodigo().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }
        modelMapper.map(clientePostPutRequestDTO, cliente);
        clienteRepository.save(cliente);
        return modelMapper.map(cliente, ClienteResponseDTO.class);
    }

    @Override
    public ClienteResponseDTO criar(ClientePostPutRequestDTO clientePostPutRequestDTO) {
        Cliente cliente = modelMapper.map(clientePostPutRequestDTO, Cliente.class);
        clienteRepository.save(cliente);
        return modelMapper.map(cliente, ClienteResponseDTO.class);
    }

    @Override
    public void remover(Long id, String codigoAcesso) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(ClienteNaoExisteException::new);
        if (!cliente.getCodigo().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }
        clienteRepository.delete(cliente);
    }

    @Override
    public List<ClienteResponseDTO> listarPorNome(String nome) {
        List<Cliente> clientes = clienteRepository.findByNomeContaining(nome);
        return clientes.stream()
                .map(ClienteResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteResponseDTO> listar() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes.stream()
                .map(ClienteResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public ClienteResponseDTO recuperar(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(ClienteNaoExisteException::new);
        return new ClienteResponseDTO(cliente);
    }
    public ClienteResponseDTO setPlanoPremium(long id ,String codigoAcesso){
        Cliente cliente = clienteRepository.findById(id).orElseThrow(ClienteNaoExisteException::new);
        cliente.setPlanoPremium(codigoAcesso);
        clienteRepository.save(cliente);
        Historico historico = criarHistorico(cliente.getId(),"Basico");
        historicoRepository.save(historico);
        return modelMapper.map(cliente, ClienteResponseDTO.class);
    }
    public ClienteResponseDTO setPlanoBasico(long id ,String codigoAcesso){
        Cliente cliente = clienteRepository.findById(id).orElseThrow(ClienteNaoExisteException::new);
        cliente.setPlanoPremium(codigoAcesso);
        clienteRepository.save(cliente);
        Historico historico = criarHistorico(cliente.getId(),"Premium");
        historicoRepository.save(historico);
        return modelMapper.map(cliente, ClienteResponseDTO.class);
    }
    private Historico criarHistorico(long idCliente, String plano){
        Historico h = new Historico();
        h.setIdCliente(idCliente);
        h.setIdPlanoAntigo(plano);
        h.setData(LocalDate.now());
        return h;
    }

}
