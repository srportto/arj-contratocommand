package br.com.srportto.contratocommand.entrypoint;

import br.com.srportto.contratocommand.application.defaultservice.cancelamento.CancelamentoOrquestradorService;
import br.com.srportto.contratocommand.domain.enums.TipoProduto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import br.com.srportto.contratocommand.application.defaultservice.contratacao.ContratacaoOrquestradorService;
import br.com.srportto.contratocommand.entrypoint.contratosrest.AutorizacaoCompletaResponseDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CancelarAutorizacaoRequestDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/autorizacao")
public class AutorizacaoController {

    private final ContratacaoOrquestradorService orquestradorContratacaoService;
    private final CancelamentoOrquestradorService orquestradorCancelamentoService;


    public AutorizacaoController(ContratacaoOrquestradorService orquestradorService, CancelamentoOrquestradorService orquestradorCancelamentoService) {
        this.orquestradorContratacaoService = orquestradorService;
        this.orquestradorCancelamentoService = orquestradorCancelamentoService;
    }

    @GetMapping("/olaMundo")
    String getOlaMundo() {
        return "Olá, mundo!";
    }

//    @GetMapping("/ativas")
//    public ResponseEntity<List<AutorizacaoCompletaResponseDto>> listarAtivas() {
//        List<AutorizacaoCompletaResponseDto> autorizacoes = pixAutoService.listarAtivas()
//                .stream()
//                .map(AutorizacaoCompletaResponseDto::from)
//                .toList();
//
//        return ResponseEntity.ok(autorizacoes);
//    }

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



    // todo -> ajustar feature de cancelamento
    @PatchMapping("/{idAutorizacao}/cancelar")
    public ResponseEntity<AutorizacaoCompletaResponseDto> cancelar(@PathVariable String idAutorizacao, @RequestHeader String tipoProduto,
            @RequestBody @Valid CancelarAutorizacaoRequestDto request) {

        request.setIdAutorizacao(idAutorizacao);
        var produto = TipoProduto.obterTipoProdutoEnumPorNome(tipoProduto);
        request.setProduto(produto);

        AutorizacaoCompletaResponseDto autorizacaoCanceladaResponse = orquestradorCancelamentoService.cancelar(request);

        return ResponseEntity.ok(autorizacaoCanceladaResponse);
    }

}
