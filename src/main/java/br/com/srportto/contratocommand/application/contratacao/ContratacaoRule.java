package br.com.srportto.contratocommand.application.contratacao;

import br.com.srportto.contratocommand.application.validationSetUp.Rule;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;

public interface ContratacaoRule extends Rule<CriarAutorizacaoRequest> {

    @Override
    default String getLogCode() {
        return "ContratacaoRule: Validando regra de negocio para criacao de autorizacao";
    }

}
