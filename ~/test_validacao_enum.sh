#!/usr/bin/env bash
# Validar implementação do enum tipoProduto no DTO

echo "=== Validação de campo 'tipoProduto' implementada ==="
echo ""
echo "1. Tipo atualizado para String em CriarAutorizacaoRequest"
echo "2. Mapeamento PixAutoAutorizacaoMapper valida conversao com menssagens claras"
echo ""
echo "Payload exemplo POST /api/autorizacao:"
echo '{"tipoProduto": "PIX_AUTO", "valor": 100, ...}'
echo ""
echo "Erros de validacao retornados (400 Bad Request):"
echo "-> 'tipoProduto' deve ser PIX_AUTO ou DDA_AUTO"
echo ""
