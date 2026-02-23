package com.ufcg.psoft.commerce.controller;

import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.EmpresaResponseDTO;
import com.ufcg.psoft.commerce.dto.PagamentoRequestDTO;
import com.ufcg.psoft.commerce.dto.PagamentoResponseDTO;
import com.ufcg.psoft.commerce.service.empresa.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<EmpresaResponseDTO> cadastrarEmpresa(@RequestBody @Valid EmpresaPostPutRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.cadastrar(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> buscarEmpresa(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.recuperar(id));
    }

    @GetMapping
    public ResponseEntity<List<EmpresaResponseDTO>> listarEmpresas() {
        return ResponseEntity.ok(empresaService.listar());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> alterarEmpresa(@PathVariable Long id, @RequestBody @Valid EmpresaPostPutRequestDTO dto) {
        return ResponseEntity.ok(empresaService.alterar(id, dto.getCodigoAcesso(), dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerEmpresa(@PathVariable Long id, @RequestBody @Valid EmpresaPostPutRequestDTO dto) {
        empresaService.remover(id, dto.getCodigoAcesso(), dto.getSenhaAdmin());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{empresaId}/tecnicos/{tecnicoId}")
    public ResponseEntity<Void> aprovarOuRejeitarTecnico(
            @PathVariable Long empresaId,
            @PathVariable Long tecnicoId,
            @RequestParam String codigoAcesso,
            @RequestParam Boolean aprovacao) {
        if (aprovacao) {
            empresaService.aprovarTecnico(empresaId, tecnicoId, codigoAcesso);
        } else {
            empresaService.rejeitarTecnico(empresaId, tecnicoId, codigoAcesso);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{empresaId}/chamados/{chamadoId}/pagamentos")
    public ResponseEntity<PagamentoResponseDTO> confirmarPagamento(
            @PathVariable Long empresaId,
            @PathVariable Long chamadoId,
            @RequestHeader String codigoAcesso,
            @RequestBody PagamentoRequestDTO pagamentoRequestDTO) {
        return ResponseEntity.ok(
                empresaService.confirmarPagamento(empresaId, chamadoId, codigoAcesso, pagamentoRequestDTO)
        );
    }

    @PutMapping("/{empresaId}/chamados/{chamadoId}/avancar-status")
    public ResponseEntity<ChamadoResponseDTO> avancarStatus(
            @PathVariable Long empresaId,
            @PathVariable Long chamadoId,
            @RequestHeader("codigoAcesso") String codigoAcesso) {
            
        ChamadoResponseDTO response = empresaService.avancarStatus(empresaId, codigoAcesso, chamadoId);
        return ResponseEntity.ok(response);
    }
}