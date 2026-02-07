package com.ufcg.psoft.commerce.controller;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.service.servico.ServicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/empresas/{empresaId}/servicos",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class ServicoController {

    @Autowired
    ServicoService servicoService;
    @GetMapping("/{id}")
    public ResponseEntity<?> recuperarServico(
            @PathVariable Long id,
            @PathVariable Long empresaId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(servicoService.recuperar(empresaId, id));
    }

    @GetMapping("")
    public ResponseEntity<?> listarServicos(
            @PathVariable Long empresaId ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(servicoService.listar(empresaId));
    }

    @PostMapping()
    public ResponseEntity<?> criarServico(
            @PathVariable Long empresaId,
            @RequestBody @Valid ServicoPostPutRequestDTO servicoPostPutRequestDto,
            @RequestHeader("codigoAcesso") String codigoAcesso)
     {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicoService.criar(servicoPostPutRequestDto,codigoAcesso, empresaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarServico(
            @PathVariable Long id,
            @PathVariable Long empresaId,
            @RequestHeader("codigoAcesso") String codigoAcesso,
            @RequestBody @Valid ServicoPostPutRequestDTO servicoPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(servicoService.alterar(empresaId,id, servicoPostPutRequestDto,codigoAcesso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirSevico(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @RequestHeader ("codigoAcesso") String codigoAcesso) {
        servicoService.remover(empresaId, id, codigoAcesso);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }

}