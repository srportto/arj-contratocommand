# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Tests

```bash
# Compilar + executar testes + gerar JAR
mvn clean package

# Rodar aplicação localmente
mvn spring-boot:run

# Executar todos os testes
mvn test

# Testar classe específica
mvn test -Dtest=ControleExpurgoAutorizacaoTest

# Testar método específico
mvn test -Dtest=ControleExpurgoAutorizacaoTest#nomeDoMetodo
```

> **Maven Wrapper quebrado no Windows**: se `./mvnw.cmd` falhar, use `mvn` diretamente.

## Pré-requisitos

- **Java 25** (JDK 25+) — usa `void main()` em vez de `public static void main()`
- **PostgreSQL 16+** com `pg_partman` e `pg_cron` — **sem fallback para H2**
- Variáveis de ambiente obrigatórias: `DB_NAME`, `DB_USER_NAME`, `DB_PASSWORD`

Docker com PostgreSQL configurado em `run_postgres16_ja_com_cron_partman/`. Exemplos de payloads REST em `docs/post-autorizacoes.txt`.

## Endpoints Reais (base: `/api/autorizacoes`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/autorizacoes` | Criar autorização (multi-produto) |
| PATCH | `/api/autorizacoes/{id}/cancelar` | Cancelar (requer header `tipoProduto`) |
| GET | `/api/autorizacoes/listar` | Listar com paginação por conta |

> **Atenção**: a URL base é `/api/autorizacoes` (plural). O README e o AGENTS.md documentam `/api/autorizacao` (sem "s"), que está **desatualizado**.

## Arquitetura

Arquitetura hexagonal com quatro camadas:

```
entrypoint/   → AutorizacaoController + DTOs (Records imutáveis)
application/  → Orquestradores, Use Cases, Mappers, Repositories
domain/       → Entidades, Enums, Utilities (sem frameworks)
shared/       → Exceções, Interceptadores, framework de validação
```

### Fluxo de uma requisição POST (criar)

```
AutorizacaoController
  └─ ContratacaoOrquestradorService          (defaultservice/contratacao)
       └─ ContratacaoService.validaContratacaoSuportada()  ← Spring injeta List<ContratacaoService>
            └─ PixAutoService                (enabledproduct/pixauto)
                 └─ CriarPixAutoUseCase
                      ├─ ContratacaoValidator.validar()    ← roda todas as ContratacaoRule
                      ├─ PixAutoMapper.toDomain()          ← MapStruct + @AfterMapping
                      │    └─ Autorizacao.inicializaCriacao()  ← gera UUID+partição, define defaults
                      └─ PixAutoRepository.save()
```

O mesmo padrão se aplica ao cancelamento via `CancelamentoOrquestradorService` + `CancelamentoService`.

### Strategy Pattern para múltiplos produtos

`ContratacaoOrquestradorService` injeta `List<ContratacaoService>` e percorre chamando `validaContratacaoSuportada(request)`. Cada serviço de produto implementa essa interface (e também `CancelamentoService`):

- `PixAutoService` → delega para `CriarPixAutoUseCase` / `CancelarPixAutoUseCase`
- `DdaAutoService` → delega para `CriarDdaAutoUseCase` / `CancelarDdaAutoUseCase`

Para adicionar um novo produto: crie um `*Service` que implemente `ContratacaoService` e/ou `CancelamentoService`, e um `Use Case` correspondente. O orquestrador o encontrará automaticamente via injeção de lista.

### Framework de validação de regras de negócio

```
Rule<T>              → interface: aceita(T) + validar(T)
ContratacaoRule      → extends Rule<CriarAutorizacaoRequest> (marker)
Validator<R,T>       → interface: getRules() + validar(T) (default que itera as regras)
ContratacaoValidator → implements Validator<ContratacaoRule, CriarAutorizacaoRequest>
                       Spring injeta List<ContratacaoRule> automaticamente
```

Para adicionar uma nova regra de negócio na criação: crie um `@Component` que implemente `ContratacaoRule`. Será injetado automaticamente no `ContratacaoValidator`.

### Particionamento temporal (crítico)

A tabela `autorizacoes` é particionada por `id_particao_conta` (range 900–999).

- **Geração da partição de escrita**: `ControleExpurgoAutorizacao.obterParticaoExpurgoWrite(dataFimVigencia)` — calcula semanas desde Epoch % 100, soma 900
- **Partição segura para drop**: `ControleExpurgoAutorizacao.obterParticaoExpurgoDrop(dataReferencia)` — lança `BusinessException` se conflitar com a partição de escrita atual
- **UUID com partição embutida**: `IdContaUUIDPartitionDistributor.getPartitionFast(idUnicoContaContratante)` + `ReversibleUUIDv7.generate(particao)` — a partição pode ser extraída depois com `ReversibleUUIDv7.extract(uuid)`, sem query adicional
- Toda essa lógica é orquestrada em `Autorizacao.inicializaCriacao()`, chamado dentro do `@AfterMapping` do MapStruct

Chave composta: `IdAutorizacao(UUID idAutorizacao, Integer idParticaoConta)` como `@EmbeddedId`. Queries por UUID sozinho usam JPQL explícito em `PixAutoRepository`.

### Mapeamento de status

O campo `status` na entidade `Autorizacao` é `Integer` (não enum): `1=ATIVO`, etc. O enum `StatusAutorizacao` existe para conversão em `ListarAutorizacoesService`.

### Exceções e códigos HTTP

| Exceção | HTTP | Quando usar |
|---------|------|-------------|
| `@Valid` em DTO | 400 | Violações de `@NotNull`, `@Min`, `@Max`, etc. |
| `BusinessException` | 422 | Regras de negócio (data no passado, produto inválido, etc.) |
| `ApplicationException` | 500 | Erros inesperados de sistema |

### Convenções de código

- DTOs são **records imutáveis** — para modificar, recriar: `new CriarAutorizacaoRequest(..., novoValor, ...)`
- Mappers com `@Mapper(componentModel = "spring")` e callbacks `@AfterMapping`
- `@Transactional` nos Use Cases (não nos Services/Orquestradores)
- Testes unitários de domínio não precisam de Spring (lógica pura em `domain/utilities/`)
