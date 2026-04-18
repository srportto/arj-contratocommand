package br.com.srportto.contratocommand.application.pixauto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import br.com.srportto.contratocommand.domain.entities.Autorizacao;
import br.com.srportto.contratocommand.domain.entities.IdAutorizacao;

@Repository
public interface PixAutoAutorizacaoRepository extends JpaRepository<Autorizacao, IdAutorizacao> {

  List<Autorizacao> findByStatus(Integer status);

}
