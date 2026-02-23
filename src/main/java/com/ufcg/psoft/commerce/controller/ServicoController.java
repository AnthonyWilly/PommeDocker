package com.ufcg.psoft.commerce.controller;
import com.ufcg.psoft.commerce.dto.ServicoFiltroDTO;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import com.ufcg.psoft.commerce.service.servico.ServicoService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class ServicoController {

    @Autowired
    ServicoService servicoService;
    @GetMapping("/empresas/{empresaId}/servicos/{id}")
    public ResponseEntity<?> recuperarServico(
            @PathVariable Long id,
            @PathVariable Long empresaId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(servicoService.recuperar(empresaId, id));
    }

    @GetMapping("/empresas/{empresaId}/servicos")
    public ResponseEntity<?> listarServicos(
            @PathVariable Long empresaId ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(servicoService.listar(empresaId));
    }

    @PostMapping("/empresas/{empresaId}/servicos")
    public ResponseEntity<?> criarServico(
            @PathVariable Long empresaId,
            @RequestBody @Valid ServicoPostPutRequestDTO servicoPostPutRequestDto,
            @RequestHeader("codigoAcesso") String codigoAcesso)
     {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicoService.criar(empresaId,codigoAcesso, servicoPostPutRequestDto));
    }

    @PutMapping("/empresas/{empresaId}/servicos/{id}")
    public ResponseEntity<?> atualizarServico(
            @PathVariable Long id,
            @PathVariable Long empresaId,
            @RequestHeader("codigoAcesso") String codigoAcesso,
            @RequestBody @Valid ServicoPostPutRequestDTO servicoPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(servicoService.alterar(empresaId,id,codigoAcesso, servicoPostPutRequestDto));
    }

    @DeleteMapping("/empresas/{empresaId}/servicos/{id}")
    public ResponseEntity<?> excluirSevico(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @RequestHeader ("codigoAcesso") String codigoAcesso) {
        servicoService.remover(empresaId, id, codigoAcesso);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }
    
    @GetMapping("/servicos/catalogo")
    public ResponseEntity<List<ServicoResponseDTO>> listarCatalogoServicoClient(
        @RequestParam Long clienteId,
        ServicoFiltroDTO filtro) {
            return ResponseEntity.ok(servicoService.listarCatalogoServicoCliente(clienteId, filtro));
    }

    @PatchMapping("/empresas/{empresaId}/servicos/{id}/disponibilidade")
    public ResponseEntity<?> alterarDisponibilidade(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @RequestHeader("codigoAcesso") String codigoAcesso,
            @RequestParam boolean disponivel) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(servicoService.alterarDisponibilidade(empresaId, id, codigoAcesso, disponivel));
    }

    @PostMapping("/servicos/{id}/interesse")
    public ResponseEntity<?> registrarInteresse(
            @PathVariable Long id,
            @RequestParam Long clienteId) {
        servicoService.registrarInteresse(clienteId, id);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

}