package br.com.srportto.contratocommand.application.usecases.ddaauto;

import org.springframework.stereotype.Service;

import br.com.srportto.contratocommand.application.defaultService.contratacao.ContratacaoService;
import br.com.srportto.contratocommand.application.usecases.ddaauto.usecases.CancelarDdaAutoUseCase;
import br.com.srportto.contratocommand.application.usecases.ddaauto.usecases.CriarDdaAutoUseCase;
import br.com.srportto.contratocommand.entrypoint.contratosrest.AutorizacaoCompletaResponseDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CancelarAutorizacaoRequest;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DdaAutoAutorizacaoService implements ContratacaoService {

    private final CriarDdaAutoUseCase criarDdaAutoUseCase;
    private final CancelarDdaAutoUseCase cancelarDdaAutoUseCase;

    @Override
    public boolean supports(CriarAutorizacaoRequest request) {
        return request.tipoProduto() != null && "DDA_AUTO".equals(request.tipoProduto().toUpperCase());
    }

    @Override
    public AutorizacaoCompletaResponseDto criarAutorizacao(CriarAutorizacaoRequest request) {
        return criarDdaAutoUseCase.executar(request);
    }

    @Override
    public AutorizacaoCompletaResponseDto cancelarAutorizacao(String idAutorizacao, CancelarAutorizacaoRequest request) {
        return cancelarDdaAutoUseCase.executar(idAutorizacao, request);
    }
}