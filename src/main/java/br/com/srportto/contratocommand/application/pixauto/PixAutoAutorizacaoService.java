package br.com.srportto.contratocommand.application.pixauto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.srportto.contratocommand.application.ContratacaoService;
import br.com.srportto.contratocommand.application.contratacao.ContratacaoValidator;
import br.com.srportto.contratocommand.domain.entities.Autorizacao;
import br.com.srportto.contratocommand.domain.entities.Cancelamento;
import br.com.srportto.contratocommand.domain.enums.TipoProduto;
import br.com.srportto.contratocommand.domain.utilities.ControleExpurgoAutorizacao;
import br.com.srportto.contratocommand.domain.utilities.ReversibleUUIDv7;
import br.com.srportto.contratocommand.entrypoint.contratosrest.AutorizacaoCompletaResponseDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CancelarAutorizacaoRequest;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import br.com.srportto.contratocommand.shared.exceptions.BusinessException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PixAutoAutorizacaoService implements ContratacaoService {

  private static final Integer STATUS_ATIVA = 1;

  private static final Logger log = LoggerFactory.getLogger(PixAutoAutorizacaoService.class);

  private final PixAutoAutorizacaoRepository repository;
  private final PixAutoAutorizacaoMapper mapper;
  private final ContratacaoValidator contratacaoValidator;

  @Override
  public boolean supports(CriarAutorizacaoRequest request) {
    // Retorna true se for um tipo de produto que o PIX Automático suporta
    return request.tipoProduto() != null && "PIX_AUTO".equals(request.tipoProduto().toUpperCase());
  }

  @Override
  public AutorizacaoCompletaResponseDto criarAutorizacao(CriarAutorizacaoRequest request) {
    return criar(request);
  }

  @Override
  public AutorizacaoCompletaResponseDto cancelarAutorizacao(String idAutorizacao, CancelarAutorizacaoRequest request) {
    return cancelar(idAutorizacao, request);
  }

  public AutorizacaoCompletaResponseDto criar(CriarAutorizacaoRequest request) {
    log.info("Iniciando criação de autorização para empresa: {}, Tipo de Produto: {}", 
        request.idAutorizacaoEmpresa(), request.tipoProduto());

    // Validar regras de negócio
    var dataFimVigenciaTratada = trataDataFimVigencia(request.dataFimVigencia(), request.codigoCanalContratacao());

    // validarDataFimVigencia(dataFimVigenciaTratada);

    CriarAutorizacaoRequest requestComDataFimTratada = new CriarAutorizacaoRequest(
        dataFimVigenciaTratada,
        request.tipoProduto(),
        request.valor(),
        request.idAutorizacaoEmpresa(),
        request.valorLimite(),
        request.frequencia(),
        request.quantidadeDividasCiclo(),
        request.indicadorUsoLimiteConta(),
        request.codigoCanalContratacao(),
        request.descricao(),
        request.idUnicoContaContratante(),
        request.idPessoaPagadora(),
        request.idPessoaDevedora(),
        request.idPessoaRecebedora(),
        request.metadados());

    validarValorLimite(request.valor(), request.valorLimite());

    // todo: evoluir para um validator generico, com injeção de regras específicas
    // por tipo de contratação
    contratacaoValidator.validar(requestComDataFimTratada);

    // Converter DTO para Entity
    Autorizacao autorizacaoMontada = mapper.toDomain(requestComDataFimTratada);

    return salvarCriacaoAutorizacao(autorizacaoMontada);

  }

  @Transactional
  private AutorizacaoCompletaResponseDto salvarCriacaoAutorizacao(Autorizacao autorizacao) {

    // Persistir no banco de dados
    Autorizacao autorizadaPersistida = persistirAutorizacao(autorizacao);

    log.info("Autorização criada com sucesso. ID: {}, Empresa: {}",
        autorizadaPersistida.getIdAutorizacao(), autorizadaPersistida.getIdAutorizacaoEmpresa());

    return AutorizacaoCompletaResponseDto.from(autorizadaPersistida);
  }

  public List<Autorizacao> listarAtivas() {
    return repository.findByStatus(STATUS_ATIVA);
  }

  private void validarValorLimite(java.math.BigDecimal valor, java.math.BigDecimal valorLimite) {
    if (valorLimite != null && valorLimite.compareTo(valor) < 0) {
      log.warn("Tentativa de criação com valorLimite inválido. Limite: {}, Valor: {}", valorLimite, valor);
      throw new BusinessException(
          "O valor limite não pode ser menor que o valor da autorização. Limite: " + valorLimite + ", Valor: " + valor);
    }
  }

  // mock para simular regra de negócio de validação da data de fim de vigência
  private LocalDate trataDataFimVigencia(LocalDate dataFimVigencia, String codigoCanalContratacao) {
    // Se dataFimVigencia foi informada, usar esse valor
    if (dataFimVigencia != null) {
      return dataFimVigencia;
    }

    // Se dataFimVigencia é nula, usar regra baseada no canal
    return switch (codigoCanalContratacao) {
      case "C1" -> LocalDate.of(9999, 1, 1); // janeiro
      case "C2" -> LocalDate.of(9999, 4, 1); // abril
      case "C3" -> LocalDate.of(9999, 7, 1); // julho
      case "C4" -> LocalDate.of(9999, 10, 1); // outubro
      case "C9" -> LocalDate.of(1990, 12, 31); // dezembro
      default -> LocalDate.of(9999, 1, 1); // Padrão: C1
    };
  }

  private Autorizacao persistirAutorizacao(Autorizacao autorizacaoMontada) {

    Autorizacao autorizadaPersistida = repository.save(autorizacaoMontada);

    log.info("Autorização persistida com sucesso. ID: {}, Empresa: {}",
        autorizadaPersistida.getIdAutorizacao(), autorizadaPersistida.getIdAutorizacaoEmpresa());

    return autorizadaPersistida;
  }

  @Transactional(readOnly = true)
  private Autorizacao obterAutorizacaoPorIdEParticao(String idAutorizacao, int idParticaoAutorizacao) {

    try {
      var idAutorizacaoUuid = UUID.fromString(idAutorizacao);
      return repository.findByIdAutorizacaoAndParticao(idAutorizacaoUuid, idParticaoAutorizacao)
          .orElseThrow(() -> new BusinessException("Autorização não encontrada com ID: " + idAutorizacao));

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new ApplicationContextException(e.getMessage());
    }
  }

  public AutorizacaoCompletaResponseDto cancelar(String idAutorizacao, CancelarAutorizacaoRequest request) {
    log.info("Iniciando cancelamento de autorização {}", idAutorizacao);

    var idParticaoAutorizacao = ReversibleUUIDv7.extract(UUID.fromString(idAutorizacao));

    var autorizacao = obterAutorizacaoPorIdEParticao(idAutorizacao, idParticaoAutorizacao);

    autorizacao.setStatus(3); // cancelada
    var dadosCancelamento = new Cancelamento();

    var dataHoraCancelamento = LocalDateTime.now();
    dadosCancelamento.setDataHoraCancelamento(dataHoraCancelamento);
    dadosCancelamento.setCodigoCanalCancelamento(request.codigoCanalCancelamento());
    dadosCancelamento.setIdPessoaCancelamento(request.idPessoaCancelamento());

    autorizacao.setDataHoraUltimaAtualizacao(dataHoraCancelamento);

    if (request.motivoCancelamento() != null) {
      dadosCancelamento.setMotivoCancelamento(request.motivoCancelamento());
    }

    autorizacao.setCancelamento(dadosCancelamento);

    // Captura partição de expurgo do momento do cancelamento
    var dataCancelamento = dataHoraCancelamento.toLocalDate();
    var particaoExpurgoWrite = ControleExpurgoAutorizacao.obterParticaoExpurgoWrite(dataCancelamento);

    // Como a chave composta foi modificada, precisamos fazer delete+insert
    // (não é possível atualizar a chave primária com JPA/Hibernate)
    var autorizacaoCanceladaEmNovaParticao = transferirParaNovaParticao(autorizacao, particaoExpurgoWrite);

    return AutorizacaoCompletaResponseDto.from(autorizacaoCanceladaEmNovaParticao);
  }

  @Transactional
  private Autorizacao transferirParaNovaParticao(Autorizacao autorizacao, Integer novaParticao) {
    UUID idAutorizacaoUuid = autorizacao.getIdAutorizacao().getIdAutorizacao();
    Integer particaoAntiga = autorizacao.getIdAutorizacao().getIdParticaoConta();

    // Se a partição não mudou, apenas persistir normalmente
    if (novaParticao.equals(particaoAntiga)) {
      return persistirAutorizacao(autorizacao);
    }

    log.info("Transferindo autorização {} da partição {} para partição {}",
        idAutorizacaoUuid, particaoAntiga, novaParticao);

    // Delete do banco com a chave antiga
    repository.deleteById(autorizacao.getIdAutorizacao());

    // Cria nova instância com a partição atualizada
    autorizacao.getIdAutorizacao().setIdParticaoConta(novaParticao);

    // Insert com a nova partição
    return persistirAutorizacao(autorizacao);
  }

}
