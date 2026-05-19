package br.com.srportto.contratocommand.application.defaultservice.contratacao;

import br.com.srportto.contratocommand.entrypoint.contratosrest.AutorizacaoCompletaResponseDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import br.com.srportto.contratocommand.shared.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratacaoOrquestradorService {

    private final List<ContratacaoService> produtosHabilitados;

    public AutorizacaoCompletaResponseDto criar(CriarAutorizacaoRequest request) {
        ContratacaoService produtoHabilitado = produtosHabilitados.stream()
                .filter(s -> s.validaServicoSuportado(request))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Produto não suportado ou inválido (tipoProduto: " + request.tipoProduto() + ")"));

        return produtoHabilitado.criarAutorizacao(request);
    }
}
