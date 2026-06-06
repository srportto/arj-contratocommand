# Atualização da Documentação para Agentes (CLAUDE.md, AGENTS.md, README.md) — Plano de Implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reescrever `CLAUDE.md` e `AGENTS.md` com conteúdo **idêntico e factualmente correto** sobre arquitetura, estrutura de pastas, fluxos e armadilhas do projeto, e corrigir as imprecisões do `README.md`, de modo que qualquer agente entenda o projeto sem ler todo o código.

**Architecture:** Um único "guia canônico para agentes" é escrito em `CLAUDE.md` e copiado byte-a-byte para `AGENTS.md` (espelhamento, decisão do usuário). O `README.md` recebe edições pontuais nas seções comprovadamente erradas. Toda referência a classe/caminho/endpoint deve corresponder ao código real verificado.

**Tech Stack:** Markdown. Verificação via `Glob`/`Grep` (caminhos e símbolos existem) e `mvn` (build sanity). Sem código de produção.

---

## Contexto: Fonte da Verdade (fatos verificados no código)

Estes fatos foram extraídos lendo o código em 2026-06-06. **Todo conteúdo escrito deve respeitá-los.** Não inventar nada além disto.

### Endpoints reais ([AutorizacaoController.java](src/main/java/br/com/srportto/contratocommand/entrypoint/AutorizacaoController.java))

Base: `@RequestMapping("/api/autorizacoes")` (plural).

| Método | Caminho | Detalhes |
|--------|---------|----------|
| POST | `/api/autorizacoes` | Body `@Valid CriarAutorizacaoRequest`; retorna `201 Created` com `AutorizacaoCompletaResponseDto` e header `Location` |
| PATCH | `/api/autorizacoes/{idAutorizacao}/cancelar` | Header obrigatório `tipoProduto`; body `CancelarAutorizacaoRequestDto`; retorna `200 OK` |
| GET | `/api/autorizacoes/listar` | Params: `idUnicoContaContratante` (UUID, obrigatório), `status` (List<String>, opcional), `pagina` (default 0), `tamanho` (default 20), `ordenarPor` (default `dataHoraInclusao,desc`); retorna `PaginacaoResponseDto<AutorizacaoResumidaResponseDto>` |

> **NÃO existem** os endpoints `/olaMundo` nem `/ativas` (citados no AGENTS/README antigos).

### Produtos suportados ([TipoProduto.java](src/main/java/br/com/srportto/contratocommand/domain/enums/TipoProduto.java))

Apenas `PIX_AUTO(1L)` e `DDA_AUTO(2L)`. **NÃO existe `CARTAO_CREDITO`.**

### Particionamento ([ControleExpurgoAutorizacao.java](src/main/java/br/com/srportto/contratocommand/domain/utilities/ControleExpurgoAutorizacao.java))

- Range de partições: **900 a 999** (`900 + (semanasDesdeEpoch % 100)`). **NÃO é 1–100.**
- `obterParticaoExpurgoWrite(LocalDate dataFinalizacao)` → int (900–999).
- `obterParticaoExpurgoDrop(LocalDate dataReferencia)` → int; lança `BusinessException` se a data está no passado ou se a partição de drop colidir com a de escrita atual.

### Entidade ([Autorizacao.java](src/main/java/br/com/srportto/contratocommand/domain/entities/Autorizacao.java))

- Pacote real: `domain.entities` (**não** `domain.model`).
- `@EmbeddedId IdAutorizacao idAutorizacao` (chave composta UUID + `idParticaoConta`).
- Campo `status` é `Integer` (`1 = ATIVO`), **não** enum. O enum `StatusAutorizacao` existe só para conversão na listagem.
- `metadados` é `String` mapeado como `jsonb` via `@JdbcTypeCode(SqlTypes.JSON)`.
- Método `inicializaCriacao(Autorizacao)` gera UUID+partição e define defaults (status=1, datas, mensageria=0, dataFimVigencia default `9999-12-31`).

### DTO de entrada ([CriarAutorizacaoRequest.java](src/main/java/br/com/srportto/contratocommand/entrypoint/contratosrest/CriarAutorizacaoRequest.java))

- `record` imutável. `tipoProduto` é **`String`** (não enum). `metadados` é `tools.jackson.databind.JsonNode` (Jackson 3).
- Validações: `@NotNull` em tipoProduto/valor/idAutorizacaoEmpresa/quantidadeDividasCiclo/indicadorUsoLimiteConta/codigoCanalContratacao/idUnicoContaContratante/idPessoaPagadora/idPessoaDevedora/idPessoaRecebedora; `@Min(1)@Max(4)` em frequencia; `@Min(1)` em quantidadeDividasCiclo. `dataFimVigencia`, `valorLimite`, `descricao`, `metadados` são opcionais.

### Estrutura de pastas real (verificada via Glob)

```
src/main/java/br/com/srportto/contratocommand/
├── ContratocommandApplication.java
├── application/
│   ├── defaultservice/
│   │   ├── contratacao/
│   │   │   ├── ContratacaoOrquestradorService.java
│   │   │   ├── ContratacaoService.java          # interface Strategy (criar)
│   │   │   ├── ContratacaoRule.java             # marker: Rule<CriarAutorizacaoRequest>
│   │   │   ├── ContratacaoValidator.java        # implements Validator<ContratacaoRule, CriarAutorizacaoRequest>
│   │   │   └── rules/
│   │   │       ├── DataFimVigenciaInvalida.java
│   │   │       ├── ValorLimiteContrato.java
│   │   │       └── MetadadoRule.java
│   │   └── cancelamento/
│   │       ├── CancelamentoOrquestradorService.java
│   │       ├── CancelamentoService.java
│   │       ├── CancelamentoRule.java
│   │       ├── CancelamentoValidator.java
│   │       └── rules/
│   │           └── TipoProdutoCancelamento.java
│   └── enabledproduct/
│       ├── pixauto/
│       │   ├── PixAutoService.java              # implements ContratacaoService, CancelamentoService
│       │   ├── PixAutoMapper.java               # MapStruct
│       │   ├── PixAutoRepository.java           # JpaRepository
│       │   ├── ListarAutorizacoesService.java
│       │   └── usecases/
│       │       ├── CriarPixAutoUseCase.java     # @Transactional
│       │       └── CancelarPixAutoUseCase.java
│       └── ddaauto/
│           ├── DdaAutoService.java
│           ├── DdaAutoMapper.java
│           ├── DdaAutoRepository.java
│           └── usecases/
│               ├── CriarDdaAutoUseCase.java
│               └── CancelarDdaAutoUseCase.java
├── domain/
│   ├── entities/        # Autorizacao, Cancelamento, IdAutorizacao
│   ├── enums/           # CanaisConhecidosEnum, MotivoStatusAutorizacao, StatusAutorizacao, TipoConta, TipoProduto
│   ├── converters/      # TipoProdutoConverter
│   ├── model/           # ContratoBase
│   └── utilities/       # AchaQtdeSemanas, ControleExpurgoAutorizacao, IdContaUUIDPartitionDistributor, ReversibleUUIDv7
├── entrypoint/
│   ├── AutorizacaoController.java
│   └── contratosrest/   # CriarAutorizacaoRequest, CancelarAutorizacaoRequestDto,
│                        # AutorizacaoCompletaResponseDto, AutorizacaoResumidaResponseDto, PaginacaoResponseDto
└── shared/
    ├── exceptions/         # ApplicationException, BusinessException
    ├── interceptors/api/   # ApiExceptionHandler, BodyOcorrenciasErrosValidations,
    │                       # LayoutErrosApiResponse, LayoutErrosApiValidationsResponse
    └── validationsetup/    # Rule, Validator
```

Testes reais:
```
src/test/java/br/com/srportto/contratocommand/
├── ContratocommandApplicationTests.java
├── application/pixauto/PixAutoAutorizacaoServiceTest.java
├── application/enabledproduct/pixauto/ListarAutorizacoesServiceTest.java
└── domain/utilities/
    ├── ControleExpurgoAutorizacaoTest.java
    └── GeraDatasPorParticao.java
```

> `docs/strategyProduto/*.java` (ProdutoStrategyFactory etc.) são **exemplos didáticos**, NÃO código de produção. Não há `ProdutoStrategyFactory` em `src/`.

### Stack ([pom.xml](pom.xml))

Java 25 · Spring Boot 4.0.4 (`spring-boot-starter-webmvc`, `-data-jpa`, `-validation`, `-test`) · Lombok 1.18.40 · MapStruct 1.5.5.Final · PostgreSQL driver (versão herdada do parent, **sem versão fixa no pom**) · Yasson 3.0.3.

---

## File Structure

| Arquivo | Responsabilidade | Ação |
|---------|------------------|------|
| `CLAUDE.md` | Guia canônico para agentes (fonte da verdade do conteúdo espelhado) | Sobrescrever |
| `AGENTS.md` | Cópia byte-a-byte de `CLAUDE.md` | Sobrescrever (via copy) |
| `README.md` | Doc público; corrigir seções factualmente erradas | Editar pontualmente |

---

### Task 1: Reescrever `CLAUDE.md` como guia canônico

**Files:**
- Modify (sobrescrever): `CLAUDE.md`

- [ ] **Step 1: Sobrescrever `CLAUDE.md` com o conteúdo canônico abaixo**

Escreva exatamente este conteúdo (é a fonte da verdade que será espelhada em AGENTS.md):

````markdown
# CLAUDE.md

> Guia para agentes de IA (Claude Code, Copilot, etc.) trabalharem neste repositório.
> **Este arquivo e `AGENTS.md` são espelhos — mantenha-os idênticos ao editar.**

API REST de **autorizações de produtos financeiros** (PIX Automático e DDA Automático), em **arquitetura hexagonal**, com **particionamento temporal** em PostgreSQL e expurgo automático de dados.

## Comece por aqui

Leia nesta ordem:
1. [AutorizacaoController.java](src/main/java/br/com/srportto/contratocommand/entrypoint/AutorizacaoController.java) — os 3 endpoints REST
2. [ContratacaoOrquestradorService.java](src/main/java/br/com/srportto/contratocommand/application/defaultservice/contratacao/ContratacaoOrquestradorService.java) — orquestração via Strategy
3. [CriarPixAutoUseCase.java](src/main/java/br/com/srportto/contratocommand/application/enabledproduct/pixauto/usecases/CriarPixAutoUseCase.java) — caso de uso completo (validação → mapper → save)
4. [Autorizacao.java](src/main/java/br/com/srportto/contratocommand/domain/entities/Autorizacao.java) — entidade de domínio com particionamento

## Build & Testes

```bash
mvn clean package                            # Compilar + testes + JAR
mvn spring-boot:run                          # Rodar localmente
mvn test                                     # Todos os testes
mvn test -Dtest=ControleExpurgoAutorizacaoTest          # Classe específica
mvn test -Dtest=ControleExpurgoAutorizacaoTest#metodo   # Método específico
```

> **Maven Wrapper quebrado no Windows**: se `./mvnw.cmd` falhar, use `mvn` diretamente.

Classes de teste existentes: `ContratocommandApplicationTests`, `PixAutoAutorizacaoServiceTest`, `ListarAutorizacoesServiceTest`, `ControleExpurgoAutorizacaoTest` (+ helper `GeraDatasPorParticao`).

## Pré-requisitos

- **Java 25** (JDK 25+) — usa `void main()` em vez de `public static void main()`
- **PostgreSQL 16+** com `pg_partman` e `pg_cron` — **sem fallback para H2**
- Variáveis de ambiente obrigatórias: `DB_NAME`, `DB_USER_NAME`, `DB_PASSWORD`
- Docker com PostgreSQL em `run_postgres16_ja_com_cron_partman/`. Exemplos de payloads em `docs/post-autorizacoes.txt`.

## Stack

| Componente | Versão | Notas |
|---|---|---|
| Java | 25 | `void main()`; records imutáveis |
| Spring Boot | 4.0.4 | Web MVC, Data JPA, Validation |
| Lombok | 1.18.40 | `@Data`, `@Getter`, `@Setter`, `@AllArgsConstructor` |
| MapStruct | 1.5.5.Final | Mapeamento DTO↔Entity com `@AfterMapping` |
| Yasson | 3.0.3 | Jakarta JSON Binding |
| PostgreSQL | 16+ | Particionamento com `pg_partman` + `pg_cron` |

> Serialização JSON usa **Jackson 3** (`tools.jackson.databind.JsonNode`).

## Endpoints reais (base `/api/autorizacoes`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/autorizacoes` | Criar autorização (multi-produto). Body `CriarAutorizacaoRequest`. → 201 |
| PATCH | `/api/autorizacoes/{idAutorizacao}/cancelar` | Cancelar. **Header obrigatório `tipoProduto`**. → 200 |
| GET | `/api/autorizacoes/listar` | Listar paginado por conta. Params: `idUnicoContaContratante`, `status`, `pagina`, `tamanho`, `ordenarPor`. → 200 |

> A base é `/api/autorizacoes` (**plural**). Não existem `/olaMundo` nem `/ativas`.

## Arquitetura (hexagonal, 4 camadas)

```
entrypoint/   → AutorizacaoController + DTOs (records imutáveis em contratosrest/)
application/  → Orquestradores, Services de produto, Use Cases, Mappers, Repositories, Validators
domain/       → Entidades, Enums, Converters, Utilities (lógica pura, sem frameworks)
shared/       → Exceções, Interceptadores (ApiExceptionHandler), framework de validação
```

`application/` divide-se em:
- `defaultservice/contratacao` e `defaultservice/cancelamento` — orquestração + framework de regras
- `enabledproduct/pixauto` e `enabledproduct/ddaauto` — implementação por produto

### Fluxo de uma requisição POST (criar)

```
AutorizacaoController.insert()
  └─ ContratacaoOrquestradorService.criar()            (defaultservice/contratacao)
       └─ percorre List<ContratacaoService> e chama validaContratacaoSuportada(request)
            └─ PixAutoService.criarAutorizacao()        (enabledproduct/pixauto)
                 └─ CriarPixAutoUseCase.execute()       (@Transactional)
                      ├─ ContratacaoValidator.validar() ← roda todas as ContratacaoRule
                      ├─ PixAutoMapper.toDomain()        ← MapStruct + @AfterMapping
                      │    └─ Autorizacao.inicializaCriacao()  ← gera UUID+partição, defaults
                      └─ PixAutoRepository.save()
```

O cancelamento segue o mesmo padrão via `CancelamentoOrquestradorService` + `CancelamentoService` + `CancelarPixAutoUseCase`.

### Strategy Pattern para múltiplos produtos

`ContratacaoOrquestradorService` injeta `List<ContratacaoService>` e seleciona o primeiro cujo `validaContratacaoSuportada(request)` retorna `true`; senão lança `BusinessException` ("Produto nao suportado").

- `PixAutoService` → `CriarPixAutoUseCase` / `CancelarPixAutoUseCase`
- `DdaAutoService` → `CriarDdaAutoUseCase` / `CancelarDdaAutoUseCase`

`PixAutoService` implementa **as duas** interfaces (`ContratacaoService` e `CancelamentoService`).

**Adicionar um produto novo**: crie um `*Service` que implemente `ContratacaoService` e/ou `CancelamentoService`, mais os Use Cases. O orquestrador o descobre automaticamente via injeção de lista. (Não há `ProdutoStrategyFactory` em `src/` — os arquivos em `docs/strategyProduto/` são apenas exemplos didáticos.)

### Framework de validação de regras de negócio

```
Rule<T>              → interface (shared/validationsetup): aceita(T) + validar(T)
Validator<R,T>       → interface: getRules() + validar(T) default que itera as regras
ContratacaoRule      → extends Rule<CriarAutorizacaoRequest> (marker)
ContratacaoValidator → implements Validator<ContratacaoRule, CriarAutorizacaoRequest>;
                       Spring injeta List<ContratacaoRule> automaticamente
```

Regras de contratação existentes (`defaultservice/contratacao/rules/`): `DataFimVigenciaInvalida`, `ValorLimiteContrato`, `MetadadoRule`.
Regra de cancelamento (`defaultservice/cancelamento/rules/`): `TipoProdutoCancelamento`.

**Adicionar regra de criação**: crie um `@Component` que implemente `ContratacaoRule` — é injetado automaticamente no `ContratacaoValidator`.

### Particionamento temporal (crítico)

Tabela `autorizacoes` particionada por `id_particao_conta` (range **900–999**).

- **Partição de escrita**: `ControleExpurgoAutorizacao.obterParticaoExpurgoWrite(dataFimVigencia)` — `900 + (semanas desde Epoch % 100)`.
- **Partição segura para drop**: `ControleExpurgoAutorizacao.obterParticaoExpurgoDrop(dataReferencia)` — lança `BusinessException` se a data está no passado ou colide com a partição de escrita atual.
- **UUID com partição embutida**: `IdContaUUIDPartitionDistributor.getPartitionFast(idUnicoContaContratante)` + `ReversibleUUIDv7.generate(particao)`. Extrai depois com `ReversibleUUIDv7.extract(uuid)`, sem query adicional.
- Tudo é orquestrado em `Autorizacao.inicializaCriacao()`, chamado no `@AfterMapping` do MapStruct.

Chave composta: `IdAutorizacao(UUID idAutorizacao, Integer idParticaoConta)` como `@EmbeddedId`. Queries só por UUID usam JPQL explícito em `PixAutoRepository`.

### Mapeamento de status

`status` na entidade `Autorizacao` é `Integer` (`1 = ATIVO`), **não** enum. O enum `StatusAutorizacao` existe para conversão em `ListarAutorizacoesService`.

### Exceções e códigos HTTP

Tratadas em `shared/interceptors/api/ApiExceptionHandler`.

| Origem | HTTP | Quando |
|--------|------|--------|
| `@Valid` em DTO | 400 | Violação de `@NotNull`, `@Min`, `@Max` |
| `BusinessException` | 422 | Regra de negócio (data no passado, produto inválido, etc.) |
| `ApplicationException` | 500 | Erro inesperado de sistema |

### Convenções

- DTOs são **records imutáveis** (`entrypoint/contratosrest/`) — para alterar, recrie: `new CriarAutorizacaoRequest(...)`. (`tipoProduto` é `String` no request; `metadados` é `JsonNode`.)
- Mappers `@Mapper(componentModel = "spring")` com callbacks `@AfterMapping`.
- `@Transactional` nos **Use Cases** (não nos Services/Orquestradores).
- Testes de domínio (`domain/utilities/`) são lógica pura, sem Spring.

## Armadilhas críticas

1. **Base de URL é `/api/autorizacoes`** (plural). README/diagramas antigos citam `/api/autorizacao`.
2. **Só existem `PIX_AUTO` e `DDA_AUTO`** — `CARTAO_CREDITO` não existe.
3. **Partições vão de 900 a 999**, não de 1 a 100.
4. **`Autorizacao` está em `domain/entities/`**, não em `domain/model/` (lá só existe `ContratoBase`).
5. **PostgreSQL obrigatório** — sem fallback H2; dialeto Hibernate específico.
6. **Records imutáveis** — não tente reatribuir campos; recrie o record.

## Documentação em `docs/`

- [info_build-my-image-and-execute.md](docs/info_build-my-image-and-execute.md) — Docker + PostgreSQL com partman/cron
- [comandos-sql.txt](docs/comandos-sql.txt) — scripts SQL de particionamento
- [post-autorizacoes.txt](docs/post-autorizacoes.txt) — exemplos de payloads REST
- [resultado-poc/POC_PARTICIONAMENTO_BUFFER_RING_UUIDV7.md](docs/resultado-poc/POC_PARTICIONAMENTO_BUFFER_RING_UUIDV7.md) — racional do particionamento
- `docs/strategyProduto/` — **exemplos didáticos** de Strategy (não é o código de produção)

## Checklist antes do commit

- [ ] `mvn test` passa
- [ ] `mvn clean compile` sem erros
- [ ] Exceções corretas: `BusinessException` (422) para regras, `ApplicationException` (500) para inesperados
- [ ] Se mexeu em particionamento, rodar `ControleExpurgoAutorizacaoTest`
- [ ] DTOs (records) recriados, não mutados
````

- [ ] **Step 2: Verificar que todos os caminhos citados existem**

Run:
```bash
mvn -q -DskipTests compile
```
Expected: BUILD SUCCESS (garante que o projeto compila; a doc só referencia código existente).

Depois, confirme manualmente que cada caminho `src/main/java/...` citado no arquivo existe (próxima task roda a verificação consolidada).

- [ ] **Step 3: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: reescreve CLAUDE.md como guia canonico e factual para agentes"
```

---

### Task 2: Espelhar conteúdo em `AGENTS.md`

**Files:**
- Modify (sobrescrever): `AGENTS.md`

- [ ] **Step 1: Copiar `CLAUDE.md` para `AGENTS.md` (cópia idêntica)**

A decisão do usuário é que os dois arquivos sejam **idênticos**. Em vez de redigitar, copie o arquivo:

```powershell
Copy-Item -Path CLAUDE.md -Destination AGENTS.md -Force
```

- [ ] **Step 2: Verificar que ficaram idênticos**

Run (PowerShell):
```powershell
if ((Get-FileHash CLAUDE.md).Hash -eq (Get-FileHash AGENTS.md).Hash) { "IDENTICOS" } else { "DIFERENTES" }
```
Expected: `IDENTICOS`

- [ ] **Step 3: Commit**

```bash
git add AGENTS.md
git commit -m "docs: espelha AGENTS.md identico ao CLAUDE.md"
```

---

### Task 3: Corrigir imprecisões factuais do `README.md`

O `README.md` tem várias seções erradas. Aplique as edições abaixo **uma a uma**, conferindo o texto-alvo antes de substituir (linhas podem deslocar entre edições).

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Corrigir a descrição do topo (produtos suportados)**

Localize na linha ~3 o trecho que cita três produtos e o Strategy via Factory. Substitua a frase sobre produtos:

De:
```
Implementa **Strategy Pattern** para múltiplos produtos (PIX Automático, DDA Automático, Cartão Crédito) e suporta **particionamento temporal** de dados em PostgreSQL.
```
Para:
```
Implementa **Strategy Pattern** para múltiplos produtos (PIX Automático e DDA Automático) e suporta **particionamento temporal** de dados em PostgreSQL.
```

- [ ] **Step 2: Corrigir lista de produtos na seção "Funcionalidades Principais"**

De:
```
- ✅ **Suporte a Múltiplos Produtos**: PIX_AUTO, DDA_AUTO, CARTAO_CREDITO (extensível)
```
Para:
```
- ✅ **Suporte a Múltiplos Produtos**: PIX_AUTO, DDA_AUTO (extensível via interface ContratacaoService)
```

E na mesma seção troque a menção a `PixAutoAutorizacaoService`:

De:
```
- ✅ **Transações PIX Automáticas**: Processamento completo via `PixAutoAutorizacaoService`
```
Para:
```
- ✅ **Transações PIX Automáticas**: Processamento completo via `PixAutoService` + `CriarPixAutoUseCase`
```

- [ ] **Step 2b: Remover/corrigir a versão fixa do driver PostgreSQL na seção "Dependências Principais"**

O pom.xml NÃO fixa versão do driver (herda do parent Spring Boot). Localize o bloco `<artifactId>postgresql</artifactId>` com `<version>42.7.1</version>` e remova a linha da versão:

De:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>
```
Para:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

- [ ] **Step 3: Substituir a árvore de "Estrutura do Projeto" pela real**

Substitua todo o bloco de código entre "## 📁 Estrutura do Projeto" e o início de "## 🏗️ Arquitetura Hexagonal" pela árvore real:

```
src/main/java/br/com/srportto/contratocommand/
├── ContratocommandApplication.java
├── application/
│   ├── defaultservice/
│   │   ├── contratacao/      # ContratacaoOrquestradorService, ContratacaoService (Strategy),
│   │   │   │                 # ContratacaoRule, ContratacaoValidator
│   │   │   └── rules/        # DataFimVigenciaInvalida, ValorLimiteContrato, MetadadoRule
│   │   └── cancelamento/     # CancelamentoOrquestradorService, CancelamentoService,
│   │       │                 # CancelamentoRule, CancelamentoValidator
│   │       └── rules/        # TipoProdutoCancelamento
│   └── enabledproduct/
│       ├── pixauto/          # PixAutoService, PixAutoMapper, PixAutoRepository, ListarAutorizacoesService
│       │   └── usecases/     # CriarPixAutoUseCase, CancelarPixAutoUseCase
│       └── ddaauto/          # DdaAutoService, DdaAutoMapper, DdaAutoRepository
│           └── usecases/     # CriarDdaAutoUseCase, CancelarDdaAutoUseCase
├── domain/
│   ├── entities/             # Autorizacao, Cancelamento, IdAutorizacao
│   ├── enums/                # TipoProduto, StatusAutorizacao, MotivoStatusAutorizacao,
│   │                         # CanaisConhecidosEnum, TipoConta
│   ├── converters/           # TipoProdutoConverter
│   ├── model/                # ContratoBase
│   └── utilities/            # ControleExpurgoAutorizacao, IdContaUUIDPartitionDistributor,
│                             # ReversibleUUIDv7, AchaQtdeSemanas
├── entrypoint/
│   ├── AutorizacaoController.java
│   └── contratosrest/        # CriarAutorizacaoRequest, CancelarAutorizacaoRequestDto,
│                             # AutorizacaoCompletaResponseDto, AutorizacaoResumidaResponseDto,
│                             # PaginacaoResponseDto
└── shared/
    ├── exceptions/           # BusinessException (422), ApplicationException (500)
    ├── interceptors/api/     # ApiExceptionHandler + DTOs de erro
    └── validationsetup/      # Rule, Validator
```

- [ ] **Step 4: Corrigir a tabela de endpoints da seção "API REST Endpoints"**

Substitua a tabela de endpoints (que cita `/olaMundo`, `/ativas`, `/api/autorizacao`) por:

```
| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| **POST** | `/api/autorizacoes` | Criar autorização (multi-produto) | 201 |
| **PATCH** | `/api/autorizacoes/{idAutorizacao}/cancelar` | Cancelar (header `tipoProduto` obrigatório) | 200 |
| **GET** | `/api/autorizacoes/listar` | Listar paginado por conta | 200 |
```

- [ ] **Step 5: Substituir os caminhos `/api/autorizacao` restantes por `/api/autorizacoes`**

Run (PowerShell) para localizar ocorrências remanescentes:
```powershell
Select-String -Path README.md -Pattern '/api/autorizacao(?!es)' -AllMatches
```
Para cada ocorrência em texto/JSON de exemplo, troque `/api/autorizacao` por `/api/autorizacoes`. Não confundir com `/api/autorizacoes` já corretos.

- [ ] **Step 6: Corrigir menções a `CARTAO_CREDITO` e `ProdutoStrategyFactory`**

Run:
```powershell
Select-String -Path README.md -Pattern 'CARTAO_CREDITO|ProdutoStrategyFactory|PixAutoAutorizacao' -AllMatches
```
Para cada ocorrência:
- Remova `CARTAO_CREDITO` das listas de produtos (deixar só `PIX_AUTO`, `DDA_AUTO`).
- Onde houver exemplo com `ProdutoStrategyFactory`, substitua pela explicação real (seleção via `List<ContratacaoService>` no `ContratacaoOrquestradorService`) ou marque como exemplo didático de `docs/strategyProduto/`.
- Troque `PixAutoAutorizacaoService`/`PixAutoAutorizacaoRepository`/`PixAutoAutorizacaoMapper` por `PixAutoService`/`PixAutoRepository`/`PixAutoMapper`.

- [ ] **Step 7: Corrigir nomes de classes de teste na seção "Estrutura de Testes"**

Substitua o bloco da árvore de testes por:
```
src/test/java/br/com/srportto/contratocommand/
├── ContratocommandApplicationTests.java
├── application/
│   ├── pixauto/PixAutoAutorizacaoServiceTest.java
│   └── enabledproduct/pixauto/ListarAutorizacoesServiceTest.java
└── domain/utilities/
    ├── ControleExpurgoAutorizacaoTest.java
    └── GeraDatasPorParticao.java
```
E nos comandos `mvn test -Dtest=...` use uma classe que existe, ex.: `mvn test -Dtest=ControleExpurgoAutorizacaoTest`.

- [ ] **Step 8: Verificação consolidada — nenhuma referência fantasma sobrou**

Run (PowerShell):
```powershell
Select-String -Path README.md,CLAUDE.md,AGENTS.md -Pattern 'CARTAO_CREDITO|ProdutoStrategyFactory|domain/model/Autorizacao|/olaMundo|/ativas|PixAutoAutorizacaoService|PixAutoAutorizacaoRepository|PixAutoAutorizacaoMapper|42\.7\.1|1\s*[-–]\s*100' -AllMatches
```
Expected: **nenhuma linha** retornada (todas as referências erradas foram eliminadas). Se algo aparecer, corrija no arquivo indicado.

- [ ] **Step 9: Commit**

```bash
git add README.md
git commit -m "docs: corrige README (endpoints, estrutura, produtos, particionamento)"
```

---

### Task 4: Verificação final dos caminhos referenciados

**Files:** (somente leitura)

- [ ] **Step 1: Confirmar que cada classe citada na doc existe no código**

Run (PowerShell) — deve retornar um caminho para cada símbolo:
```powershell
@('AutorizacaoController','ContratacaoOrquestradorService','CancelamentoOrquestradorService',
  'ContratacaoService','CancelamentoService','ContratacaoValidator','ContratacaoRule',
  'PixAutoService','PixAutoMapper','PixAutoRepository','ListarAutorizacoesService',
  'CriarPixAutoUseCase','CancelarPixAutoUseCase','DdaAutoService',
  'CriarDdaAutoUseCase','CancelarDdaAutoUseCase',
  'Autorizacao','Cancelamento','IdAutorizacao','TipoProduto','StatusAutorizacao',
  'ControleExpurgoAutorizacao','IdContaUUIDPartitionDistributor','ReversibleUUIDv7',
  'CriarAutorizacaoRequest','CancelarAutorizacaoRequestDto','AutorizacaoCompletaResponseDto',
  'AutorizacaoResumidaResponseDto','PaginacaoResponseDto',
  'BusinessException','ApplicationException','ApiExceptionHandler','Rule','Validator') |
  ForEach-Object {
    $n=$_; $f = Get-ChildItem -Recurse -Filter "$n.java" src 2>$null
    if (-not $f) { "FALTANDO: $n" } else { "ok: $n" }
  }
```
Expected: nenhuma linha começando com `FALTANDO:`.

- [ ] **Step 2: Build de sanidade**

Run:
```bash
mvn -q -DskipTests compile
```
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit final (se houver ajuste)**

```bash
git add -A
git commit -m "docs: verificacao final das referencias da documentacao de agentes"
```

---

## Self-Review (executado na redação do plano)

- **Cobertura do pedido**: descrever projeto ✅ (topo), arquitetura ✅ (camadas + fluxo + Strategy + validação), estrutura de pastas ✅ (árvore real verificada), build/test/endpoints/armadilhas ✅. CLAUDE.md e AGENTS.md idênticos ✅ (Task 2 copia). README corrigido ✅ (Task 3).
- **Placeholders**: nenhum — todo conteúdo de CLAUDE.md está escrito por extenso; AGENTS.md é cópia; README usa edições com texto de/para explícito.
- **Consistência de nomes**: `PixAutoService` (não `PixAutoAutorizacaoService`), `domain/entities` (não `domain/model`), range `900–999`, base `/api/autorizacoes`, produtos `PIX_AUTO`/`DDA_AUTO` — uniformes em todas as tasks e cobertos pelo grep da Task 3 Step 8 e Task 4 Step 1.
