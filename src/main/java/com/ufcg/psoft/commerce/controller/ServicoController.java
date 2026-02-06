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
        value = "/servicos",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class ServicoController {

    @Autowired
    ServicoService servicoService;
    @GetMapping("/{id}")
    public ResponseEntity<?> recuperarServico(
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(servicoService.recuperar(id));
    }

    @GetMapping("")
    public ResponseEntity<?> listarServicos(
            @RequestParam(required = false, defaultValue = "") String nome) {

        if (nome != null && !nome.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(servicoService.listarPorNome(nome));
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(servicoService.listar());
    }

    @PostMapping()
    public ResponseEntity<?> criarServico(
            @RequestBody @Valid ServicoPostPutRequestDTO servicoPostPutRequestDto,
            @RequestHeader("codigo-acesso") String codigoAcesso)
     {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicoService.criar(servicoPostPutRequestDto,codigoAcesso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarServico(
            @PathVariable Long id,
            @RequestHeader("codigo-acesso") String codigoAcesso,
            @RequestBody @Valid ServicoPostPutRequestDTO servicoPostPutRequestDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(servicoService.alterar(id, servicoPostPutRequestDto,codigoAcesso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirSevico(
            @PathVariable Long idServico,
            @RequestParam Long empresaId,
            @RequestHeader ("codigo-acesso") String codigoAcesso) {
        servicoService.remover(empresaId, idServico, codigoAcesso);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }

}