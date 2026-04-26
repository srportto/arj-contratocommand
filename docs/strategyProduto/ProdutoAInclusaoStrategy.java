package br.com.srportto.contratocommand.application.produto;

import org.springframework.stereotype.Component;

import br.com.srportto.contratocommand.domain.model.Produto;

@Component("PRODUTO_A")
public class ProdutoAInclusaoStrategy implements ProdutoStrategy {
    @Override
    public void incluirProduto(Produto produto) {
        // Regras específicas para Produto A
        // Exemplo: produto.setDataFimVigencia(null);
    }
}
