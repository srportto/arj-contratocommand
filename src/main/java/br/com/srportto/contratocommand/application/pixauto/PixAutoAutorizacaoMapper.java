package br.com.srportto.contratocommand.application.pixauto;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.srportto.contratocommand.domain.entities.Autorizacao;
import br.com.srportto.contratocommand.domain.entities.IdAutorizacao;
import br.com.srportto.contratocommand.domain.enums.TipoProduto;
import br.com.srportto.contratocommand.domain.utilities.IdContaUUIDPartitionDistributor;
import br.com.srportto.contratocommand.domain.utilities.ReversibleUUIDv7;
import br.com.srportto.contratocommand.entrypoint.contratosrest.CriarAutorizacaoRequest;
import br.com.srportto.contratocommand.shared.exceptions.BusinessException;

@Mapper(componentModel = "spring")
public interface PixAutoAutorizacaoMapper {

    @Mapping(source = "valor", target = "valorAutorizacao")
    @Mapping(source = "frequencia", target = "frequenciaPagamento")
    @Mapping(source = "quantidadeDividasCiclo", target = "quantidadeDividasCiclo")
    @Mapping(source = "indicadorUsoLimiteConta", target = "indicadorUsoLimiteConta")
    @Mapping(target = "idAutorizacao", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "motivoStatus", ignore = true)
    @Mapping(target = "dataInicioVigencia", ignore = true)
    @Mapping(source = "dataFimVigencia", target = "dataFimVigencia")
    @Mapping(target = "dataHoraInclusao", ignore = true)
    @Mapping(target = "dataHoraUltimaAtualizacao", ignore = true)
    @Mapping(target = "indicadorTipoMensageria", ignore = true)
    @Mapping(target = "cancelamento", ignore = true)
    @Mapping(target = "metadados", ignore = true)
    Autorizacao toDomain(CriarAutorizacaoRequest request);

    @AfterMapping
    default void afterMapping(CriarAutorizacaoRequest request, @MappingTarget Autorizacao autorizacao) {

        // Validação e conversão de tipoProduto: String -> enum TipoProduto
        var tipoStr = request.tipoProduto();
        if (!"PIX_AUTO".equals(tipoStr.toUpperCase()) && !"DDA_AUTO".equals(tipoStr.toUpperCase())) {
            throw new BusinessException(
                "O campo 'tipoProduto' é inválido. Valores permitidos: PIX_AUTO, DDA_AUTO." +
                " Valor recebido: '" + tipoStr + "'"
            );
        }

        autorizacao.setTipoProduto(TipoProduto.valueOf(tipoStr.toUpperCase()));

        // Preenchimento PK e valores padrão para criação de nova autorização
        var idUnicoContaContratante = autorizacao.getIdUnicoContaContratante();
        var idParticaoConta = IdContaUUIDPartitionDistributor.getPartitionFast(idUnicoContaContratante);
        var idAutorizacao = ReversibleUUIDv7.generate(idParticaoConta);

        autorizacao.setIdAutorizacao(new IdAutorizacao());
        autorizacao.getIdAutorizacao().setIdAutorizacao(idAutorizacao);
        autorizacao.getIdAutorizacao().setIdParticaoConta(idParticaoConta);

        autorizacao.setStatus(1); // ATIVO
        autorizacao.setMotivoStatus("Autorizacao criada com sucesso");
        autorizacao.setDataInicioVigencia(LocalDate.now());

        LocalDateTime agora = LocalDateTime.now();
        autorizacao.setDataHoraInclusao(agora);
        autorizacao.setDataHoraUltimaAtualizacao(agora);

        autorizacao.setIndicadorTipoMensageria((byte) 0); // não utiliza mensageria

        if (request.metadados() != null) {
            autorizacao.setMetadados(request.metadados().toString());
        } else {
            autorizacao.setMetadados("{}");
        }
    }

}
