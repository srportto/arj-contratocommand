package br.com.srportto.contratocommand.application.contratacao.rules;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

import br.com.srportto.contratocommand.application.contratacao.ContratacaoRule;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import br.com.srportto.contratocommand.shared.exceptions.BusinessException;

@Component
public class DataFimVigenciaInvalida implements ContratacaoRule {

    @Override
    public boolean aceita(CriarAutorizacaoRequest request) {
        return true;
    }

    @Override
    public void validar(CriarAutorizacaoRequest request) {
        var dataFimVigencia = request.dataFimVigencia();

        if (dataFimVigencia.isBefore(LocalDate.now())) {
            throw new BusinessException(
                    "A data de fim de vigência não pode ser no passado. Data informada: " + dataFimVigencia);
        }
    }

}
