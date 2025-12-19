package com.ufcg.psoft.commerce.controller;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.service.empresa.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Empresa> cadastrarEmpresa(@RequestBody @Valid EmpresaPostPutRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.cadastrar(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> buscarEmpresa(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.recuperar(id));
    }

    @GetMapping
    public ResponseEntity<List<Empresa>> listarEmpresas() {
        return ResponseEntity.ok(empresaService.listar());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> alterarEmpresa(@PathVariable Long id, @RequestBody @Valid EmpresaPostPutRequestDTO dto) {
        return ResponseEntity.ok(empresaService.alterar(id, dto.getCodigoAcesso(), dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerEmpresa(@PathVariable Long id, @RequestBody @Valid EmpresaPostPutRequestDTO dto) {
        empresaService.remover(id, dto.getCodigoAcesso(), dto.getSenhaAdmin());
        return ResponseEntity.noContent().build();
    }
}