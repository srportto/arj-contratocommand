package br.com.srportto.contratocommand.entrypoint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import br.com.srportto.contratocommand.application.pixauto.PixAutoAutorizacaoService;
import br.com.srportto.contratocommand.domain.entities.Autorizacao;
import br.com.srportto.contratocommand.entrypoint.contratosrest.AutorizacaoCompletaResponseDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/autorizacao")
public class AutorizacaoController {

  private final PixAutoAutorizacaoService service;

  public AutorizacaoController(PixAutoAutorizacaoService service) {
    this.service = service;
  }

  @GetMapping("/olaMundo")
  String getOlaMundo() {
    return "Olá, mundo!";
  }

  @GetMapping("/ativas")
  public ResponseEntity<List<AutorizacaoCompletaResponseDto>> listarAtivas() {
    List<AutorizacaoCompletaResponseDto> autorizacoes = service.listarAtivas()
        .stream()
        .map(AutorizacaoCompletaResponseDto::from)
        .toList();

    return ResponseEntity.ok(autorizacoes);
  }

  @PostMapping
  public ResponseEntity<AutorizacaoCompletaResponseDto> insert(@RequestBody @Valid CriarAutorizacaoRequest requestRecord) {
    Autorizacao autorizada = service.criar(requestRecord);

    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(autorizada.getIdAutorizacao())
            .toUri();

    return ResponseEntity.created(uri).body(AutorizacaoCompletaResponseDto.from(autorizada));
  }



}
