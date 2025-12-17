package com.ufcg.psoft.commerce.controller;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.service.empresa.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<?> criarEmpresa(@RequestBody EmpresaPostPutRequestDTO dto) {
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarEmpresa(@PathVariable Long id) {
        return null;
    }

    @GetMapping
    public ResponseEntity<?> listarEmpresas() {
        return null;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> alterarEmpresa(@PathVariable Long id, @RequestBody EmpresaPostPutRequestDTO dto) {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerEmpresa(@PathVariable Long id, @RequestBody EmpresaPostPutRequestDTO dto) {
        return null;
    }
}