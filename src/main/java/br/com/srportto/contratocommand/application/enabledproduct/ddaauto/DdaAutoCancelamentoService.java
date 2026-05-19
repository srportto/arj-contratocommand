package br.com.srportto.contratocommand.application.enabledproduct.ddaauto;

import br.com.srportto.contratocommand.application.defaultservice.cancelamento.CancelamentoService;
import br.com.srportto.contratocommand.application.defaultservice.contratacao.ContratacaoService;
import br.com.srportto.contratocommand.application.enabledproduct.ddaauto.usecases.CancelarDdaAutoUseCase;
import br.com.srportto.contratocommand.application.enabledproduct.ddaauto.usecases.CriarDdaAutoUseCase;
import br.com.srportto.contratocommand.domain.enums.TipoProduto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.AutorizacaoCompletaResponseDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CancelarAutorizacaoRequestDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DdaAutoCancelamentoService implements CancelamentoService {


    private final CancelarDdaAutoUseCase cancelarDdaAutoUseCase;

    @Override
    public boolean validaServicoSuportado(CancelarAutorizacaoRequestDto request) {
        return request.getProduto() != null && TipoProduto.DDA_AUTO.name().equalsIgnoreCase(request.getProduto().name());
    }

    @Override
    public AutorizacaoCompletaResponseDto cancelarAutorizacao(CancelarAutorizacaoRequestDto request) {
        return cancelarDdaAutoUseCase.executar(request);
    }


}