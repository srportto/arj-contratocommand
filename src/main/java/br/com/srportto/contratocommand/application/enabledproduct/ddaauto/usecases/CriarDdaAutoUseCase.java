package br.com.srportto.contratocommand.application.enabledproduct.ddaauto.usecases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import br.com.srportto.contratocommand.application.defaultservice.contratacao.ContratacaoValidator;
import br.com.srportto.contratocommand.application.enabledproduct.ddaauto.DdaAutoAutorizacaoMapper;
import br.com.srportto.contratocommand.application.enabledproduct.ddaauto.DdaAutoAutorizacaoRepository;
import br.com.srportto.contratocommand.domain.entities.Autorizacao;
import br.com.srportto.contratocommand.entrypoint.contratosrest.AutorizacaoCompletaResponseDto;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import br.com.srportto.contratocommand.shared.exceptions.BusinessException;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CriarDdaAutoUseCase {

    private static final Logger log = LoggerFactory.getLogger(CriarDdaAutoUseCase.class);

    private final DdaAutoAutorizacaoRepository repository;
    private final DdaAutoAutorizacaoMapper mapper;
    private final ContratacaoValidator contratacaoValidator;

    @Transactional
    public AutorizacaoCompletaResponseDto executar(CriarAutorizacaoRequest request) {
        log.info("Iniciando criação de autorização DDA para empresa: {}", request.idAutorizacaoEmpresa());

        var dataFimVigenciaTratada = trataDataFimVigencia(request.dataFimVigencia(), request.codigoCanalContratacao());

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

        contratacaoValidator.validar(requestComDataFimTratada);

        Autorizacao autorizacaoMontada = mapper.toDomain(requestComDataFimTratada);
        
        Autorizacao autorizadaPersistida = repository.save(autorizacaoMontada);

        log.info("Autorização DDA criada com sucesso. ID: {}, Empresa: {}", 
                autorizadaPersistida.getIdAutorizacao().getIdAutorizacao(), 
                autorizadaPersistida.getIdAutorizacaoEmpresa());

        return AutorizacaoCompletaResponseDto.from(autorizadaPersistida);
    }

    private void validarValorLimite(java.math.BigDecimal valor, java.math.BigDecimal valorLimite) {
        if (valorLimite != null && valorLimite.compareTo(valor) < 0) {
            log.warn("Tentativa de criação com valorLimite inválido. Limite: {}, Valor: {}", valorLimite, valor);
            throw new BusinessException(
                    "O valor limite não pode ser menor que o valor da autorização. Limite: " + valorLimite + ", Valor: " + valor);
        }
    }

    private LocalDate trataDataFimVigencia(LocalDate dataFimVigencia, String codigoCanalContratacao) {
        if (dataFimVigencia != null) {
            return dataFimVigencia;
        }

        return switch (codigoCanalContratacao) {
            case "C1" -> LocalDate.of(9999, 1, 1);
            case "C2" -> LocalDate.of(9999, 4, 1);
            case "C3" -> LocalDate.of(9999, 7, 1);
            case "C4" -> LocalDate.of(9999, 10, 1);
            case "C9" -> LocalDate.of(1990, 12, 31);
            default -> LocalDate.of(9999, 1, 1);
        };
    }
}