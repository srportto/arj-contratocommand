package br.com.srportto.contratocommand.application.produto;

import org.springframework.stereotype.Component;

import br.com.srportto.contratocommand.domain.model.Produto;

@Component("PRODUTO_B")
public class ProdutoBInclusaoStrategy implements ProdutoStrategy {
    @Override
    public void incluirProduto(Produto produto) {
        // Regras específicas para Produto B
        // Exemplo: validar data de fim de vigência
    }
}
