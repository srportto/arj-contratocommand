package br.com.srportto.contratocommand.domain.enums;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public enum TipoProduto {
    PIX_AUTO(1L),
    DDA_AUTO(2L);

    private long tipoProduto;

    TipoProduto(long tipoProduto) {
        this.tipoProduto = tipoProduto;
    }

    public long getTipoProduto() {
        return this.tipoProduto;
    }

    public static TipoProduto obterTipoProdutoEnumPorId(long tipoProdutoId) {
        for (TipoProduto tipoEnum : TipoProduto.values()) {
            if (tipoEnum.getTipoProduto() == tipoProdutoId) {
                return tipoEnum;
            }
        }
        throw new IllegalArgumentException(
                String.format("Tipo de produto %d não conhecido ", tipoProdutoId));
    }

    public static TipoProduto obterTipoProdutoEnumPorNome(String nomeProduto) {
        for (TipoProduto tipoEnum : TipoProduto.values()) {
            if (tipoEnum.name().equalsIgnoreCase(nomeProduto)) {
                return tipoEnum;
            }
        }
        throw new IllegalArgumentException(
                String.format("Produto %d não conhecido ", nomeProduto));
    }
}
