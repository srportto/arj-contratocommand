package br.com.srportto.contratocommand.application.pixauto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import br.com.srportto.contratocommand.domain.entities.Autorizacao;

@Repository
public interface PixAutoAutorizacaoRepository extends JpaRepository<Autorizacao, UUID> {

  List<Autorizacao> findByStatus(Integer status);

}
