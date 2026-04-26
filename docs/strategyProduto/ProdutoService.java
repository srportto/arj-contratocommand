package br.com.srportto.contratocommand.application.produto;

import br.com.srportto.contratocommand.domain.model.Produto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {
    private final ProdutoStrategyFactory strategyFactory;

    public ProdutoService(ProdutoStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @Transactional
    public void incluirProduto(String tipoProduto, Produto produto) {
        ProdutoStrategy strategy = strategyFactory.getStrategy(tipoProduto);
        strategy.incluirProduto(produto);
        // Persistir produto, se necessário
    }
}
