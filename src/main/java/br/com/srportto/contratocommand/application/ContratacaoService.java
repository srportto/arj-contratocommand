package br.com.srportto.contratocommand.application;

import br.com.srportto.contratocommand.entrypoint.contratosrest.AutorizacaoCompletaResponseDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;

public interface ContratacaoService {

   default AutorizacaoCompletaResponseDto criarAutorizacao(CriarAutorizacaoRequest request) {
      throw new UnsupportedOperationException("Metodo criarAutorizacao nao implementado");
   }

}
