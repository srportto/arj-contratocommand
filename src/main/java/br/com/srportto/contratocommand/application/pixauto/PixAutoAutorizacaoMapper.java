package br.com.srportto.contratocommand.application.pixauto;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.srportto.contratocommand.domain.entities.Autorizacao;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;

@Mapper(componentModel = "spring")
public interface PixAutoAutorizacaoMapper {

  @Mapping(source = "valor", target = "valorAutorizacao")
  @Mapping(source = "frequencia", target = "frequenciaPagamento")
  @Mapping(source = "quantidadeDividasCiclo", target = "quantidadeDividasCiclo")
  @Mapping(source = "indicadorUsoLimiteConta", target = "indicadorUsoLimiteConta")
  @Mapping(target = "idAutorizacao", ignore = true)
  @Mapping(target = "status", ignore = true) // Será definido no @AfterMapping
  @Mapping(target = "motivoStatus", ignore = true) // Será definido no @AfterMapping
  @Mapping(target = "dataInicioVigencia", ignore = true) // Será definido no @AfterMapping
  @Mapping(target = "dataFimVigencia", ignore = true) // Será definido no @AfterMapping com regra de negócio
  @Mapping(target = "dataHoraInclusao", ignore = true) // Será definido no @AfterMapping
  @Mapping(target = "dataHoraUltimaAtualizacao", ignore = true) // Será definido no @AfterMapping
  @Mapping(target = "indicadorTipoMensageria", ignore = true) // Será definido no @AfterMapping
  @Mapping(target = "cancelamento", ignore = true) // Será null na criação
  @Mapping(target = "metadados", ignore = true) // Será convertido no @AfterMapping
  Autorizacao toDomain(CriarAutorizacaoRequest request);

  /**
   * Define valores padrão e conversões customizadas após o mapeamento automático.
   * Implementa a regra de negócio para preenchimento de dataFimVigencia:
   * - Se dataFimVigencia for preenchida, usar esse valor
   * - Se dataFimVigencia for nula, usar data padrão baseada em codigoCanalContratacao
   */
  @AfterMapping
  default void afterMapping(CriarAutorizacaoRequest request, @MappingTarget Autorizacao autorizacao) {
    // Valores defaults para criação de nova autorização
    autorizacao.setStatus(1); // 1 = ATIVA
    autorizacao.setMotivoStatus("Autorização criada com sucesso");
    autorizacao.setDataInicioVigencia(LocalDate.now());
    
    // Regra de negócio para dataFimVigencia
    autorizacao.setDataFimVigencia(preencherDataFimVigencia(request.dataFimVigencia(), request.codigoCanalContratacao()));
    
    LocalDateTime agora = LocalDateTime.now();
    autorizacao.setDataHoraInclusao(agora);
    autorizacao.setDataHoraUltimaAtualizacao(agora);
    
    autorizacao.setIndicadorTipoMensageria((short) 0); // 0 = não utiliza mensageria
    
    // Converter JsonNode metadados para String
    if (request.metadados() != null) {
      autorizacao.setMetadados(request.metadados().toString());
    } else {
      autorizacao.setMetadados("{}"); // Padrão vazio
    }
  }

  /**
   * Preenche a data de fim de vigência conforme regra de negócio.
   *
   * @param dataFimVigencia Data fornecida no request (pode ser null)
   * @param codigoCanalContratacao Código do canal de contratação
   * @return Data de fim de vigência a ser persistida
   */
  private LocalDate preencherDataFimVigencia(LocalDate dataFimVigencia, String codigoCanalContratacao) {
    // Se dataFimVigencia foi informada, usar esse valor
    if (dataFimVigencia != null) {
      return dataFimVigencia;
    }

    // Se dataFimVigencia é nula, usar regra baseada no canal
    return switch (codigoCanalContratacao) {
      case "C1" -> LocalDate.of(9999, 1, 1);    // janeiro
      case "C2" -> LocalDate.of(9999, 4, 1);    // abril
      case "C3" -> LocalDate.of(9999, 7, 1);    // julho
      case "C4" -> LocalDate.of(9999, 10, 1);   // outubro
      default -> LocalDate.of(9999, 1, 1); // Padrão: C1
    };
  }
}
