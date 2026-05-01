package br.com.srportto.contratocommand.application;

import br.com.srportto.contratocommand.entrypoint.contratosrest.AutorizacaoCompletaResponseDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CancelarAutorizacaoRequest;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import br.com.srportto.contratocommand.shared.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratacaoOrquestradorService {

    private final List<ContratacaoService> servicosProduto;

    public AutorizacaoCompletaResponseDto criar(CriarAutorizacaoRequest request) {
        ContratacaoService servico = servicosProduto.stream()
                .filter(s -> s.supports(request))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Produto não suportado ou inválido (tipoProduto: " + request.tipoProduto() + ")"));

        return servico.criarAutorizacao(request);
    }

    public AutorizacaoCompletaResponseDto cancelar(String idAutorizacao, CancelarAutorizacaoRequest request) {
        // Se quisermos validar pelo idAutorizacao, podemos precisar buscar os dados no banco
        // antes de saber de qual produto é. Por enquanto, como o cancelamento depende do produto,
        // vamos fixar para usar o primeiro da lista, ou no futuro você pode passar o produto no CancelarAutorizacaoRequest.
        // Como o sistema atual só tem PIX Automático, chamaremos o primeiro.
        if (servicosProduto.isEmpty()) {
            throw new BusinessException("Nenhum serviço de produto configurado.");
        }

        // Temporário até termos diferenciação por produto no cancelamento
        return servicosProduto.get(0).cancelarAutorizacao(idAutorizacao, request);
    }
}
