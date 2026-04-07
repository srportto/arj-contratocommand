package br.com.srportto.contratocommand.shared.exceptions;

public class BusinessException extends RuntimeException {

  // toda vez que uma regra de negocios for violada, deve ser lançada uma
  // BusinessException, com a mensagem explicando o motivo da violação
  public BusinessException(String message) {
    super(message);
  }

}
