package br.com.srportto.contratocommand.domain.entities;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "autorizacoes") // tabela que guarda as roles/perfis de usuarios conhecidos da aplicacao
public class Autorizacao {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_autorizacao", nullable = false, unique = true, length = 36)
  private UUID idAutorizacao;


  //passa a ser opcional no request, quando nao informada usar o campo canal para decidir o que persisir na base de dados
  //quando canal igual C1 , salvar  9999-01-01
  //quando canal igual C2 , salvar  9999-04-01
  //quando canal igual C3 , salvar  9999-07-01
  //quando canal igual C4 , salvar  9999-10-01
  @Column(name = "data_fim_vigencia", nullable = false)
  private LocalDate dataFimVigencia;

  @Column(name = "status", nullable = false)
  private Integer status;

  @Column(name = "motivo_status", nullable = false)
  private String motivoStatus;

  @Column(name = "data_inicio_vigencia", nullable = false)
  private LocalDate dataInicioVigencia;

  @Column(name = "data_hora_inclusao", nullable = false)
  private LocalDateTime dataHoraInclusao;

  @Column(name = "data_hora_ultima_atlz", nullable = false)
  private LocalDateTime dataHoraUltimaAtualizacao;

  @Column(name = "valor", nullable = false, precision = 17, scale = 2)
  private BigDecimal valorAutorizacao;

  @Column(name = "id_autorizacao_empresa", nullable = false, unique = false)
  private String idAutorizacaoEmpresa;

  @Column(name = "valor_limite", nullable = false, precision = 17, scale = 2)
  private BigDecimal valorLimite;

  @Column(name = "frequencia", nullable = false)
  private short frequenciaPagamento; // 1 - mensal, 2 - bimestral, 3 - trimestral, 4 - semestral, 5 - anual

  @Column(name = "quantidade_dividas_ciclo", nullable = false)
  private short quantidadeDividasCiclo;

  @Column(name = "indicador_uso_limite_conta", nullable = false)
  private short indicadorUsoLimiteConta; // 0 - nao utiliza limite de conta, 1 - utiliza limite de conta

  @Column(name = "indicador_tipo_mensageria ", nullable = false)
  private short indicadorTipoMensageria; // 0 - nao utiliza mensageria, 1 - utiliza mensageria SPI , 2 ...

  @Column(name = "codigo_canal_contratacao", nullable = false)
  private String codigoCanalContratacao; // C1 - canal presencial, C2 - canal digital, C3 - canal central de atendimento

  @Column(name = "descricao", nullable = true)
  private String descricao;

  @Column(name = "id_unico_conta_contratante", nullable = false, unique = false, length = 36)
  private UUID idUnicoContaContratante;

  @Column(name = "id_pessoa_pagadora", nullable = false, unique = false, length = 36)
  private UUID idPessoaPagadora;

  @Column(name = "id_pessoa_devedora", nullable = false, unique = false, length = 36)
  private UUID idPessoaDevedora;

  @Column(name = "id_pessoa_recebedora", nullable = false, unique = false, length = 36)
  private UUID idPessoaRecebedora;

  @Embedded
  private Cancelamento cancelamento;

  @Column(name = "metadados", nullable = false, unique = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String metadados;

  public java.util.UUID getIdAutorizacao() {
    return idAutorizacao;
  }

  public String getIdAutorizacaoEmpresa() {
    return idAutorizacaoEmpresa;
  }

  public void setIdAutorizacao(java.util.UUID idAutorizacao) {
    this.idAutorizacao = idAutorizacao;
  }

  public void setDataFimVigencia(LocalDate dataFimVigencia) {
    this.dataFimVigencia = dataFimVigencia;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public void setMotivoStatus(String motivoStatus) {
    this.motivoStatus = motivoStatus;
  }

  public void setDataInicioVigencia(LocalDate dataInicioVigencia) {
    this.dataInicioVigencia = dataInicioVigencia;
  }

  public void setDataHoraInclusao(LocalDateTime dataHoraInclusao) {
    this.dataHoraInclusao = dataHoraInclusao;
  }

  public void setDataHoraUltimaAtualizacao(LocalDateTime dataHoraUltimaAtualizacao) {
    this.dataHoraUltimaAtualizacao = dataHoraUltimaAtualizacao;
  }

  public void setValorAutorizacao(BigDecimal valorAutorizacao) {
    this.valorAutorizacao = valorAutorizacao;
  }

  public void setIdAutorizacaoEmpresa(String idAutorizacaoEmpresa) {
    this.idAutorizacaoEmpresa = idAutorizacaoEmpresa;
  }

  public void setValorLimite(BigDecimal valorLimite) {
    this.valorLimite = valorLimite;
  }

  public void setFrequenciaPagamento(short frequenciaPagamento) {
    this.frequenciaPagamento = frequenciaPagamento;
  }

  public void setQuantidadeDividasCiclo(short quantidadeDividasCiclo) {
    this.quantidadeDividasCiclo = quantidadeDividasCiclo;
  }

  public void setIndicadorUsoLimiteConta(short indicadorUsoLimiteConta) {
    this.indicadorUsoLimiteConta = indicadorUsoLimiteConta;
  }

  public void setIndicadorTipoMensageria(short indicadorTipoMensageria) {
    this.indicadorTipoMensageria = indicadorTipoMensageria;
  }

  public void setCodigoCanalContratacao(String codigoCanalContratacao) {
    this.codigoCanalContratacao = codigoCanalContratacao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public void setIdUnicoContaContratante(UUID idUnicoContaContratante) {
    this.idUnicoContaContratante = idUnicoContaContratante;
  }

  public void setIdPessoaPagadora(UUID idPessoaPagadora) {
    this.idPessoaPagadora = idPessoaPagadora;
  }

  public void setIdPessoaDevedora(UUID idPessoaDevedora) {
    this.idPessoaDevedora = idPessoaDevedora;
  }

  public void setIdPessoaRecebedora(UUID idPessoaRecebedora) {
    this.idPessoaRecebedora = idPessoaRecebedora;
  }

  public void setCancelamento(Cancelamento cancelamento) {
    this.cancelamento = cancelamento;
  }

  public void setMetadados(String metadados) {
    this.metadados = metadados;
  }
}

