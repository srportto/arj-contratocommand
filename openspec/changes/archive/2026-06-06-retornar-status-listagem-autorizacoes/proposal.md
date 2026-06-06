## Why

O endpoint `GET /api/autorizacoes/listar` permite **filtrar** por status, mas a resposta resumida (`AutorizacaoResumidaResponseDto`) **não devolve o status** de cada autorização. O consumidor não consegue distinguir uma autorização `ATIVA` de uma `CANCELADA`/`EXPIRADA` sem fazer uma chamada adicional, o que torna a listagem incompleta para qualquer tela que precise exibir a situação atual do contrato.

## What Changes

- Adicionar o campo `status` (String, nome do enum `StatusAutorizacao`, ex.: `"ATIVA"`) ao DTO de resposta resumida da listagem.
- Preencher esse campo na conversão entidade → DTO, traduzindo o `status` (Integer) da entidade `Autorizacao` para o nome do enum via `StatusAutorizacao.obterStatusEnumPorIdStatus(...)`.
- Atualizar os testes de `ListarAutorizacoesService` para asseverar que o `status` é retornado corretamente.
- Não há mudança de contrato de entrada nem nos filtros — apenas enriquecimento da resposta (adição de campo, retrocompatível).

## Capabilities

### New Capabilities
- `listagem-autorizacoes`: Listagem paginada de autorizações por conta, com filtros de status, ordenação e a representação dos campos resumidos retornados — incluindo agora o status textual de cada autorização.

### Modified Capabilities
<!-- Nenhuma capability de requisitos pré-existente em openspec/specs/. -->

## Impact

- **DTO**: `entrypoint/contratosrest/AutorizacaoResumidaResponseDto.java` — novo campo `status` e preenchimento em `from(...)`.
- **Domínio (consumido, não alterado)**: `domain/enums/StatusAutorizacao` (já possui `obterStatusEnumPorIdStatus`).
- **Testes**: `ListarAutorizacoesServiceTest` — nova asserção sobre `status`.
- **API**: resposta de `GET /api/autorizacoes/listar` ganha um campo adicional. Mudança aditiva e retrocompatível (sem breaking change).
- **Sem impacto** em particionamento, expurgo, persistência ou contratos de criação/cancelamento.
