package br.com.srportto.contratocommand.application;

import br.com.srportto.contratocommand.entrypoint.contratosrest.AutorizacaoCompletaResponseDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CancelarAutorizacaoRequest;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;

public interface ContratacaoService {

   boolean supports(CriarAutorizacaoRequest request);

   default AutorizacaoCompletaResponseDto criarAutorizacao(CriarAutorizacaoRequest request) {
      throw new UnsupportedOperationException("Método criarAutorizacao não implementado");
   }

   default AutorizacaoCompletaResponseDto cancelarAutorizacao(String idAutorizacao, CancelarAutorizacaoRequest request) {
      throw new UnsupportedOperationException("Método cancelarAutorizacao não implementado");
   }

}
