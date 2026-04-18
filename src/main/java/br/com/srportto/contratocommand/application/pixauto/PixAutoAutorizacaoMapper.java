package br.com.srportto.contratocommand.application.pixauto;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.srportto.contratocommand.domain.entities.Autorizacao;
import br.com.srportto.contratocommand.domain.entities.IdAutorizacao;
import br.com.srportto.contratocommand.domain.utilities.IdContaUUIDPartitionDistributor;
import br.com.srportto.contratocommand.domain.utilities.ReversibleUUIDv7;
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
  @Mapping(source = "dataFimVigencia", target = "dataFimVigencia")
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
    

    //preenchimento PK da entidade (idAutorizacao + particao)
    var idUnicoContaContratante = autorizacao.getIdUnicoContaContratante();
    System.out.println("ID Único Conta Contratante: " + idUnicoContaContratante); // Log para verificar o valor

    var idParticaoConta = IdContaUUIDPartitionDistributor.getPartitionFast(idUnicoContaContratante);  
  System.out.println("ID Partição Conta gerado: " + idParticaoConta); // Log para verificar o valor


    var idAutorizacao = ReversibleUUIDv7.generate(idParticaoConta);
    System.out.println("UUID Autorização gerado: " + idAutorizacao); // Log para verificar o valor
    System.out.println("ID Partição Conta extraído do UUID: " + ReversibleUUIDv7.extract(idAutorizacao)); // Log para verificar a extração da partição

    // Inicializar IdAutorizacao antes de usar
    autorizacao.setIdAutorizacao(new IdAutorizacao());
    autorizacao.getIdAutorizacao().setIdAutorizacao(idAutorizacao);
    autorizacao.getIdAutorizacao().setIdParticaoConta(idParticaoConta);

    // Preenchendo demais valores padroes para criação de nova autorização
    autorizacao.setStatus(1); // 1 = ATIVA
    autorizacao.setMotivoStatus("Autorização criada com sucesso");
    autorizacao.setDataInicioVigencia(LocalDate.now());
    
    
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


}
