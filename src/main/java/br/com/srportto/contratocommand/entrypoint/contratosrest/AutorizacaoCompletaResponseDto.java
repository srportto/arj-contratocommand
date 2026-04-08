package br.com.srportto.contratocommand.entrypoint.contratosrest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.srportto.contratocommand.domain.entities.Autorizacao;
import br.com.srportto.contratocommand.domain.entities.Cancelamento;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;

public record AutorizacaoCompletaResponseDto(
    UUID idAutorizacao,
    LocalDate dataFimVigencia,
    Integer status,
    String motivoStatus,
    LocalDate dataInicioVigencia,
    LocalDateTime dataHoraInclusao,
    LocalDateTime dataHoraUltimaAtualizacao,
    BigDecimal valorAutorizacao,
    String idAutorizacaoEmpresa,
    BigDecimal valorLimite,
    Short frequenciaPagamento,
    Short quantidadeDividasCiclo,
    Short indicadorUsoLimiteConta,
    Short indicadorTipoMensageria,
    String codigoCanalContratacao,
    String descricao,
    UUID idUnicoContaContratante,
    UUID idPessoaPagadora,
    UUID idPessoaDevedora,
    UUID idPessoaRecebedora,
    Cancelamento cancelamento,
    JsonNode metadados) {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static AutorizacaoCompletaResponseDto from(Autorizacao autorizacao) {
    return new AutorizacaoCompletaResponseDto(
        autorizacao.getIdAutorizacao(),
        autorizacao.getDataFimVigencia(),
        autorizacao.getStatus(),
        autorizacao.getMotivoStatus(),
        autorizacao.getDataInicioVigencia(),
        autorizacao.getDataHoraInclusao(),
        autorizacao.getDataHoraUltimaAtualizacao(),
        autorizacao.getValorAutorizacao(),
        autorizacao.getIdAutorizacaoEmpresa(),
        autorizacao.getValorLimite(),
        autorizacao.getFrequenciaPagamento(),
        autorizacao.getQuantidadeDividasCiclo(),
        autorizacao.getIndicadorUsoLimiteConta(),
        autorizacao.getIndicadorTipoMensageria(),
        autorizacao.getCodigoCanalContratacao(),
        autorizacao.getDescricao(),
        autorizacao.getIdUnicoContaContratante(),
        autorizacao.getIdPessoaPagadora(),
        autorizacao.getIdPessoaDevedora(),
        autorizacao.getIdPessoaRecebedora(),
        autorizacao.getCancelamento(),
        parseMetadados(autorizacao.getMetadados()));
  }

  private static JsonNode parseMetadados(String metadados) {
    if (metadados == null || metadados.isBlank()) {
      return JsonNodeFactory.instance.objectNode();
    }

    try {
      return OBJECT_MAPPER.readTree(metadados);
    } catch (Exception e) {
      return JsonNodeFactory.instance.objectNode();
    }
  }
}
