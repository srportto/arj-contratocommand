package br.com.srportto.contratocommand.entrypoint;

import br.com.srportto.contratocommand.application.defaultservice.cancelamento.CancelamentoOrquestradorService;
import br.com.srportto.contratocommand.domain.enums.TipoProduto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import br.com.srportto.contratocommand.application.defaultservice.contratacao.ContratacaoOrquestradorService;
import br.com.srportto.contratocommand.application.enabledproduct.pixauto.PixAutoAutorizacaoService;
import br.com.srportto.contratocommand.entrypoint.contratosrest.AutorizacaoCompletaResponseDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CancelarAutorizacaoRequestDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/autorizacao")
public class AutorizacaoController {

    private final ContratacaoOrquestradorService orquestradorContratacaoService;
    private final CancelamentoOrquestradorService orquestradorCancelamentoService;
    private final PixAutoAutorizacaoService pixAutoService; // Mantido para o listarAtivas

    public AutorizacaoController(ContratacaoOrquestradorService orquestradorService, PixAutoAutorizacaoService pixAutoService) {
        this.orquestradorContratacaoService = orquestradorService;
        this.pixAutoService = pixAutoService;
    }

    @GetMapping("/olaMundo")
    String getOlaMundo() {
        return "Olá, mundo!";
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<AutorizacaoCompletaResponseDto>> listarAtivas() {
        List<AutorizacaoCompletaResponseDto> autorizacoes = pixAutoService.listarAtivas()
                .stream()
                .map(AutorizacaoCompletaResponseDto::from)
                .toList();

        return ResponseEntity.ok(autorizacoes);
    }

    @PostMapping
    public ResponseEntity<AutorizacaoCompletaResponseDto> insert(
            @RequestBody @Valid CriarAutorizacaoRequest requestRecord) {
        AutorizacaoCompletaResponseDto autorizadaResponse = orquestradorContratacaoService.criar(requestRecord);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(autorizadaResponse.getIdAutorizacao())
                .toUri();

        return ResponseEntity.created(uri).body(autorizadaResponse);
    }

    @PatchMapping("/{idAutorizacao}/cancelar")
    public ResponseEntity<AutorizacaoCompletaResponseDto> cancelar(@PathVariable String idAutorizacao, @PathVariable String tipoProduto,
            @RequestBody @Valid CancelarAutorizacaoRequestDto request) {

        request.setIdAutorizacao(idAutorizacao);
        var produto = TipoProduto.obterTipoProdutoEnumPorNome(tipoProduto);
        request.setProduto(produto);

        AutorizacaoCompletaResponseDto autorizacaoCanceladaResponse = orquestradorCancelamentoService.cancelar(request);

        return ResponseEntity.ok(autorizacaoCanceladaResponse);
    }

}
