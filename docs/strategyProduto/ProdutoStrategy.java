package br.com.srportto.contratocommand.application.produto;

import br.com.srportto.contratocommand.domain.model.Produto;

public interface ProdutoStrategy {
    void incluirProduto(Produto produto);
}