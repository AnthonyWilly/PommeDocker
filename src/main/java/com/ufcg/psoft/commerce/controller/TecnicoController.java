package com.ufcg.psoft.commerce.controller;

import com.ufcg.psoft.commerce.dto.TecnicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.model.StatusDisponibilidade;
import com.ufcg.psoft.commerce.service.tecnico.TecnicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
    value = "/tecnicos",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class TecnicoController {

    @Autowired
    TecnicoService tecnicoService;

    @GetMapping("/{id}")
    public ResponseEntity<?> recuperarTecnico(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(
            tecnicoService.recuperar(id)
        );
    }

    @GetMapping
    public ResponseEntity<?> listarTecnicos() {
        return ResponseEntity.status(HttpStatus.OK).body(
            tecnicoService.listar()
        );
    }

    @PostMapping
    public ResponseEntity<?> criarTecnico(
        @RequestBody @Valid TecnicoPostPutRequestDTO tecnicoDTO
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            tecnicoService.criar(tecnicoDTO)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> alterarTecnico(
        @PathVariable Long id,
        @RequestParam("acesso") String acesso,
        @RequestBody @Valid TecnicoPostPutRequestDTO tecnicoDTO
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            tecnicoService.alterar(id, acesso, tecnicoDTO)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerTecnico(
        @PathVariable Long id,
        @RequestParam("acesso") String acesso
    ) {
        tecnicoService.remover(id, acesso);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<?> alterarDisponibilidade(
        @PathVariable Long id,
        @RequestParam("acesso") String acesso,
        @RequestParam("status") StatusDisponibilidade novoStatus
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            tecnicoService.alterarDisponibilidade(id, acesso, novoStatus)
        );
    }
}
