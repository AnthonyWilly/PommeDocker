package com.ufcg.psoft.commerce.controller;

import com.ufcg.psoft.commerce.dto.ChamadoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.dto.EmpresaResponseDTO;
import com.ufcg.psoft.commerce.model.ChamadoStatus;
import com.ufcg.psoft.commerce.service.chamado.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @PostMapping("/clientes/{clienteId}/chamados")
    public ResponseEntity<ChamadoResponseDTO> criarChamado(
            @PathVariable Long clienteId,
            @RequestHeader("codigoAcesso") String codigoAcesso,
            @RequestBody @Valid ChamadoPostPutRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(chamadoService.criarChamado(clienteId, codigoAcesso, dto));
    }

    @PutMapping("/chamados/{chamadoId}/pagamento")
    public ResponseEntity<ChamadoResponseDTO> confirmarPagamento(
            @PathVariable Long chamadoId,
            @RequestHeader("codigoAcesso") String codigoAcesso,
            @RequestParam String metodoPagamento) {
        return ResponseEntity
                .ok(chamadoService.confirmarPagamento(chamadoId, codigoAcesso, metodoPagamento));
    }
    @DeleteMapping("/chamados/{chamadoId}")
    public ResponseEntity<Void> removerChamado(
            @PathVariable Long chamadoId,
            @RequestHeader("codigoAcesso") String codigoAcesso) {
        chamadoService.removerChamado(chamadoId, codigoAcesso);
        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("clientes/{clienteId}/chamados/{chamadoId}")
    public ResponseEntity<ChamadoResponseDTO> buscarChamadoCliente(
            @PathVariable Long clienteId,
            @PathVariable Long chamadoId,
            @RequestHeader String codigoAcesso) {
        return ResponseEntity.ok(chamadoService.buscarChamadoPorCliente(chamadoId, clienteId, codigoAcesso));

    }

    @GetMapping("clientes/{clienteId}/chamados")
    public ResponseEntity<List<ChamadoResponseDTO>> listarTodosChamados(
            @PathVariable Long clienteId,
            @RequestHeader String codigoAcesso) {
        return ResponseEntity.ok(chamadoService.listarChamadosCliente(clienteId, codigoAcesso));
    }

    @GetMapping("clientes/{clienteId}/chamados/status")
    public ResponseEntity<List<ChamadoResponseDTO>> listarChamadosClienteStatus(
            @PathVariable Long clienteId,
            @RequestParam(required = false) ChamadoStatus status,
            @RequestHeader String codigoAcesso) {
        return ResponseEntity.ok(chamadoService.listarChamadosClientePorStatus(clienteId,status, codigoAcesso));

    }

    @DeleteMapping("/clientes/{clienteId}/chamados/{chamadoId}")
    public ResponseEntity<Void> cancelarChamado(
            @PathVariable Long clienteId,
            @PathVariable Long chamadoId,
            @RequestHeader("codigoAcesso") String codigoAcesso) {
        chamadoService.cancelar(chamadoId, clienteId, codigoAcesso);
        return ResponseEntity
                .noContent()
                .build();
    }
}
    @PatchMapping("/clientes/{clienteId}/chamados/{chamadoId}/confirmar-conclusao")
    public ResponseEntity<ChamadoResponseDTO> confirmarConclusao(
            @PathVariable Long clienteId,
            @PathVariable Long chamadoId,
            @RequestHeader("codigoAcesso") String codigoAcesso) {
        return ResponseEntity.ok(chamadoService.confirmarConclusao(clienteId, codigoAcesso, chamadoId));
    }
}
