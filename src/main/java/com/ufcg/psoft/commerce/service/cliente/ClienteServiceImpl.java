package com.ufcg.psoft.commerce.service.cliente;
import com.ufcg.psoft.commerce.dto.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ClienteResponseDTO;
import com.ufcg.psoft.commerce.exception.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.CommerceException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.HistoricoPlanoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    ModelMapper modelMapper;
    @Autowired
    private HistoricoPlanoRepository  historicoRepository;


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
        cliente.setPlanoAtual(Plano.BASICO);
        cliente.setProxPlano(null);
        cliente.setDataCobranca(LocalDate.now().plusDays(30));
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

    private ClienteResponseDTO alterarPlano(Long id, String codigoAcesso, String novoPlano) {
        Cliente cliente = buscarValidandoAcesso(id, codigoAcesso);
        Plano planoEnum;
        try {
            planoEnum = Plano.valueOf(novoPlano.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CommerceException("Plano não encontrado: " + novoPlano);
        }
        HistoricoPlano historico = criarHistorico(cliente.getId(), cliente.getPlanoAtual());
        historicoRepository.save(historico);
        cliente.setProxPlano(planoEnum);
        clienteRepository.save(cliente);
        return modelMapper.map(cliente, ClienteResponseDTO.class);
    }

    @Override
    public ClienteResponseDTO setPlanoPremium(Long id, String codigoAcesso){
        return alterarPlano(id, codigoAcesso, "Premium");
    }

    @Override
    public ClienteResponseDTO setPlanoBasico(Long id, String codigoAcesso){
        return alterarPlano(id, codigoAcesso, "Basico");
    }

    private HistoricoPlano criarHistorico(long idCliente, Plano plano) {
        HistoricoPlano h = new HistoricoPlano();
        h.setIdCliente(idCliente);
        h.setPlanoAntigo(plano);
        h.setData(LocalDate.now());
        return h;
    }

    private Cliente buscarValidandoAcesso(Long id, String codigoAcesso) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(ClienteNaoExisteException::new);
        if (!cliente.getCodigo().equals(codigoAcesso))
            throw new CodigoDeAcessoInvalidoException();
        return cliente;
    }

}