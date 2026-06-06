# listagem-autorizacoes

## Purpose

Listagem paginada de autorizações por conta (endpoint `GET /api/autorizacoes/listar`), com filtros de status, ordenação e os campos resumidos retornados para cada autorização.

## Requirements

### Requirement: Resposta da listagem inclui o status da autorização

A resposta resumida de cada autorização retornada por `GET /api/autorizacoes/listar` SHALL incluir um campo `status` do tipo String, contendo o nome do enum `StatusAutorizacao` correspondente ao código de status persistido na entidade (ex.: `"ATIVA"`, `"CANCELADA"`).

O valor SHALL ser derivado do campo `status` (Integer) da entidade `Autorizacao` por meio do mapeamento código → enum, garantindo que cada item listado exiba sua situação atual sem necessidade de uma requisição adicional.

#### Scenario: Autorização ativa retornada com status textual

- **WHEN** o consumidor lista autorizações de uma conta e existe uma autorização cujo status persistido é o código de `ATIVA`
- **THEN** o item correspondente na resposta SHALL conter o campo `status` com o valor `"ATIVA"`

#### Scenario: Autorização cancelada retornada com status textual

- **WHEN** o consumidor lista autorizações de uma conta e existe uma autorização cujo status persistido é o código de `CANCELADA`
- **THEN** o item correspondente na resposta SHALL conter o campo `status` com o valor `"CANCELADA"`

#### Scenario: Cada item da página exibe seu próprio status

- **WHEN** a página de resultados contém múltiplas autorizações com status distintos
- **THEN** cada item SHALL refletir, no campo `status`, o nome do enum correspondente ao seu próprio código de status persistido
