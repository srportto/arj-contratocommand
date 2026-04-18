package br.com.srportto.contratocommand.application.pixauto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import br.com.srportto.contratocommand.domain.entities.Autorizacao;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import br.com.srportto.contratocommand.shared.exceptions.BusinessException;

@Service
public class PixAutoAutorizacaoService {

  private static final Integer STATUS_ATIVA = 1;

  private static final Logger log = LoggerFactory.getLogger(PixAutoAutorizacaoService.class);

  private final PixAutoAutorizacaoRepository repository;
  private final PixAutoAutorizacaoMapper mapper;

  public PixAutoAutorizacaoService(PixAutoAutorizacaoRepository repository, PixAutoAutorizacaoMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  /**
   * Cria uma nova autorização a partir do payload recebido.
   * Executa validações de regra de negócio antes de persistir.
   *
   * @param request Dados da autorização a criar
   * @return Autorização persistida com ID preenchido
   * @throws BusinessException Se as validações de negócio falharem
   */
  @Transactional
  public Autorizacao criar(CriarAutorizacaoRequest request) {
    log.info("Iniciando criação de autorização para empresa: {}", request.idAutorizacaoEmpresa());

    // Validar regras de negócio
    var dataFimVigenciaTratada = trataDataFimVigencia(request.dataFimVigencia(), request.codigoCanalContratacao());
    validarDataFimVigencia(dataFimVigenciaTratada);

    CriarAutorizacaoRequest requestComDataFimTratada = new CriarAutorizacaoRequest(
        dataFimVigenciaTratada,
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

    // Converter DTO para Entity
    Autorizacao autorizacao = mapper.toDomain(requestComDataFimTratada);

    // Persistir no banco de dados
    Autorizacao autorizada = repository.save(autorizacao);

    log.info("Autorização criada com sucesso. ID: {}, Empresa: {}",
        autorizada.getIdAutorizacao(), autorizada.getIdAutorizacaoEmpresa());

    return autorizada;
  }

  public List<Autorizacao> listarAtivas() {
    return repository.findByStatus(STATUS_ATIVA);
  }

  /**
   * Valida se a data de fim de vigência é válida (não pode ser no passado).
   *
   * @param dataFimVigencia Data a validar
   * @throws BusinessException Se a data for no passado
   */
  private void validarDataFimVigencia(LocalDate dataFimVigencia) {

    LocalDate hoje = LocalDate.now();
    if (dataFimVigencia.isBefore(hoje)) {
      log.warn("Tentativa de criação com data de fim de vigência no passado: {}", dataFimVigencia);
      throw new BusinessException(
          "A data de fim de vigência não pode ser no passado. Data informada: " + dataFimVigencia);
    }
  }

  /**
   * Valida se o valor limite é compatível com o valor da autorização.
   * Se valorLimite for informado, deve ser maior ou igual ao valor.
   *
   * @param valor       Valor da autorização
   * @param valorLimite Valor limite (opcional)
   * @throws BusinessException Se valorLimite < valor
   */
  private void validarValorLimite(java.math.BigDecimal valor, java.math.BigDecimal valorLimite) {
    if (valorLimite != null && valorLimite.compareTo(valor) < 0) {
      log.warn("Tentativa de criação com valorLimite inválido. Limite: {}, Valor: {}", valorLimite, valor);
      throw new BusinessException(
          "O valor limite não pode ser menor que o valor da autorização. Limite: " + valorLimite + ", Valor: " + valor);
    }
  }

  /**
   * Preenche a data de fim de vigência conforme regra de negócio.
   *
   * @param dataFimVigencia        Data fornecida no request (pode ser null)
   * @param codigoCanalContratacao Código do canal de contratação
   * @return Data de fim de vigência a ser persistida
   */
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
      case "C9" -> LocalDate.of(9999, 12, 31); // dezembro
      default -> LocalDate.of(9999, 1, 1); // Padrão: C1
    };
  }
}
