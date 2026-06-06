## Context

A listagem `GET /api/autorizacoes/listar` é servida por `ListarAutorizacoesService`, que converte cada `Autorizacao` em `AutorizacaoResumidaResponseDto` via o factory estático `AutorizacaoResumidaResponseDto.from(autorizacao)`. Hoje o DTO expõe `idAutorizacao`, datas de vigência, recebedor, valor e metadado — mas **não** o status.

Na entidade, `status` é um `Integer` (código). O enum `domain/enums/StatusAutorizacao` já mapeia código ↔ nome e oferece `obterStatusEnumPorIdStatus(long)`, que lança `IllegalArgumentException` para códigos desconhecidos. O consumidor da listagem optou por receber o **nome do enum** (String), e não o código bruto.

## Goals / Non-Goals

**Goals:**
- Expor o status de cada autorização na resposta da listagem como String com o nome do enum (ex.: `"ATIVA"`).
- Mudança aditiva e retrocompatível no contrato de saída.
- Cobertura de teste asseverando o status retornado.

**Non-Goals:**
- Não alterar filtros de entrada, paginação ou ordenação (o filtro `status` já existe).
- Não retornar código numérico nem descrição adicional do status.
- Não alterar persistência, particionamento, expurgo ou contratos de criação/cancelamento.
- Não integrar `nomeRecebedor` (segue como placeholder).

## Decisions

**Decisão 1 — Representação como nome do enum (String).**
O campo `status` será uma String com `StatusAutorizacao.name()`. Alternativas consideradas: (a) código inteiro bruto — rejeitado por exigir que o consumidor conheça o mapeamento; (b) objeto `{codigo, descricao}` — rejeitado por adicionar complexidade desnecessária ao caso de uso atual. O nome do enum é autoexplicativo e estável.

**Decisão 2 — Conversão dentro de `AutorizacaoResumidaResponseDto.from(...)`.**
A tradução código → nome ocorrerá no mesmo factory que já monta o DTO, mantendo a responsabilidade de mapeamento concentrada e o `ListarAutorizacoesService` inalterado. Usa-se `StatusAutorizacao.obterStatusEnumPorIdStatus(autorizacao.getStatus()).name()`.

**Decisão 3 — Tratamento de status nulo/desconhecido.**
Se `status` da entidade for nulo, o campo `status` do DTO será `null` (sem exceção). Para um código presente mas não mapeado, `obterStatusEnumPorIdStatus` lança `IllegalArgumentException`; como todo dado persistido provém do domínio (códigos válidos do enum), esse caso é considerado invariante de dados e não é tratado defensivamente na listagem.

## Risks / Trade-offs

- **[Código persistido fora do range do enum]** → `obterStatusEnumPorIdStatus` lançaria `IllegalArgumentException` durante a listagem. Mitigação: os códigos são escritos exclusivamente pelo próprio domínio a partir do enum; nenhuma origem externa grava `status`. Caso surja necessidade futura, encapsular a conversão com fallback.
- **[Acoplamento DTO → enum de domínio]** → o DTO de entrypoint passa a referenciar `StatusAutorizacao`. Trade-off aceito: o enum é estável e o `from(...)` já depende da entidade de domínio.
