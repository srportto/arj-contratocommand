package br.com.srportto.contratocommand.entrypoint.contratosrest;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CancelarAutorizacaoRequest(
  
    @NotNull(message = "o campo 'codigoCanalCancelamento' é obrigatorio.")
    String codigoCanalCancelamento,

    @NotNull  (message = "O campo 'idPessoaCancelamento' é obrigatório.")
    UUID idPessoaCancelamento,

    String motivoCancelamento

) {
}
