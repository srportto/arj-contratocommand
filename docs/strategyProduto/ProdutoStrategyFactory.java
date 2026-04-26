package br.com.srportto.contratocommand.application.produto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProdutoStrategyFactory {
    private final Map<String, ProdutoStrategy> strategyMap;

    @Autowired
    public ProdutoStrategyFactory(Map<String, ProdutoStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    public ProdutoStrategy getStrategy(String tipoProduto) {
        ProdutoStrategy strategy = strategyMap.get(tipoProduto);
        if (strategy == null) {
            throw new IllegalArgumentException("Tipo de produto não suportado: " + tipoProduto);
        }
        return strategy;
    }
}
