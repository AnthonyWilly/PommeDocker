package com.ufcg.psoft.commerce.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufcg.psoft.commerce.dto.ServicoFiltroDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import com.ufcg.psoft.commerce.service.servico.ServicoService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(
    value = "/servico",
    produces = MediaType.APPLICATION_JSON_VALUE
)

public class ServicoController {
    
    @Autowired
    ServicoService servicoService;

    @GetMapping
    public ResponseEntity<List<ServicoResponseDTO>> listarCatalogoServicoClient(
        @RequestParam Long clienteId, 
        ServicoFiltroDTO filtro) {
            return ResponseEntity.ok(servicoService.listarCatalogoServicoCliente(clienteId, filtro));
    }
    
}
