# Contrato Command

API REST Java 25 para gerenciamento de autorizações de contratos e processamento de operações PIX automáticas com arquitetura hexagonal. Implementa Strategy Pattern para múltiplos produtos (PIX, Cartão Crédito) e suporta particionamento temporal de dados PostgreSQL.

## 📋 Sobre o Projeto

O **Contrato Command** é um microserviço backend construído com Spring Boot 4.0.4 seguindo princípios de Domain-Driven Design (DDD) e arquitetura hexagonal. Fornece APIs REST para:
- **Autorizações**: Criação, listagem e cancelamento
- **Transações PIX Automáticas**: Processamento via PixAutoAutorizacaoService
- **Multiplidade de Produtos**: Strategy Pattern com interface `ContratacaoService` (PIX_AUTO, CARTAO_CREDITO)
- **Particionamento Temporal**: Suporte a partições de dados PostgreSQL 12+ via pg_partman + pg_cron

### Funcionalidades Principais
- ✅ Criação de autorizações com validação robusta (Min, Max, NotNull)
- ✅ Processamento de transações PIX automáticas (PixAutoAutorizacaoService)
- ✅ Multiplidade de produtos via Strategy Pattern (PIX, Cartão Crédito)
- ✅ Cancelamento programado de autorizações
- ✅ Listagem de autorizações ativas com metadados JSONB
- ✅ Persistência em PostgreSQL 16+ com particionamento temporal
- ✅ Validação customizada com Jakarta Bean Validation 3.0
- ✅ Tratamento centralizado de exceções (BusinessException, ApplicationException)
- ✅ Logging estruturado e tratamento de erros HTTP
- ✅ DTOs imutáveis com records Java e Builder pattern

## 🛠️ Tecnologias

### Core Framework
- **Spring Boot** `4.0.4` - Framework web, IoC e autowiring
- **Spring Data JPA** - Persistência de dados com Hibernate ORM
- **Spring Validation** - Validação de entrada com Jakarta Bean Validation
- **Spring Web MVC** - Construção de APIs REST

### Linguagem & Compilação
- **Java** `25` - Com preview features (void main, records)
- **Maven** - Gerenciamento de dependências e build
- **Lombok** `1.18.40` - Redução de boilerplate (@Data, @Getter, @Setter)

### Padrões & Mapeamento
- **MapStruct** `1.5.5.Final` - Mapeamento automático DTO ↔ Entity com @AfterMapping
- **Jakarta Validation** - Validação com custom validators (Min, Max, NotNull)
- **Yasson** `3.0.3` - Serialização JSON via Jakarta JSON Binding

### Padrões de Projeto
- **Strategy Pattern** - Interface `ContratacaoService` para produtos PIX/Cartão via ProdutoStrategyFactory
- **Repository Pattern** - Abstração JPA com queries customizadas
- **DTO Pattern** - Records imutáveis e Builder para transferência segura de dados
- **Mapper Pattern** - Mapeamento automático entre camadas

### Banco de Dados
- **PostgreSQL** `16+` - Persistência relacional com particionamento temporal
- **Hibernate** - Dialeto PostgreSQLDialect para JPA

### Serialização
- **Yasson** `3.0.3` - Serialização/desserialização JSON (Jakarta JSON Binding)

## 📁 Estrutura do Projeto

```
src/main/java/br/com/srportto/contratocommand/
├── ContratocommandApplication.java          # Classe principal Spring Boot
│
├── application/                              # Layer Application (Orquestração)
│   ├── ContratacaoService.java              # Strategy Interface para produtos
│   ├── ContratacaoOrquestradorService.java  # Orquestrador de criação/cancelamento
│   │
│   └── pixauto/                             # Caso de uso PIX Auto
│       ├── PixAutoAutorizacaoService.java   # Orquestração de negócio completa
│       ├── PixAutoAutorizacaoRepository.java# JPA Repository com queries customizadas
│       └── PixAutoAutorizacaoMapper.java    # MapStruct mapper DTO ↔ Entity + @AfterMapping
│
├── domain/                                   # Domain Layer (Lógica pura)
│   ├── entities/                            # Entidades de domínio
│   │   ├── Autorizacao.java                 # Entidade com composite PK (UUID + partição)
│   │   └── Cancelamento.java                # Entidade de cancelamento
│   │
│   ├── enums/                               # Enums do domínio
│   │   ├── Tipos/                           # Estratégia multi-produto
│   │   │   └── tipoEnum/TipoProduto.java    # PIX_AUTO, CARTAO_CREDITO, etc
│   │   ├─ StatusAutorizacao.java            # ATIVO, CANCELADO, EXPIRADO
│   │   ├── Motivos/motivoStatusEnum/        # Transições de estado
│   │   │   └── MotivoStatusAutorizacao.java
│   │   └── CanaisConhecidosEnum.java        # Canais de contratação conhecidos
│   │
│   └── model/                               # Modelos de negócio puros
│       ├── ContratoBase.java                # Modelo base abstrato
│       └── Autorizacao.java                 # Modelo alternativo sem composite PK
│
├── entrypoint/                               # Layer Entrypoint (REST)
│   ├── AutorizacaoController.java           # Main controller com endpoints /olaMundo, /ativas, POST, PATCH
│   └── contratosrest/                       # DTOs de transferência de dados
│       ├── CriarAutorizacaoRequest.java     # Record imutável com validações (Min, Max, NotNull)
│       ├── CancelarAutorizacaoRequest.java  # Request para cancelamento programado
│       ├── AutorizacaoCompletaResponseDto.java # DTO completo para resposta JSONB + metadados
│       └── componentescontrato/              # Componentes específicos (se existirem)
│
└── shared/                                    # Shared Layer (Infraestrutura)
    ├── configurations/                       # Configurações Spring, autoconfigurations
    ├── exceptions/                          # Exceções customizadas
    │   ├── BusinessException.java           # 422 - Violação de regra de negócio
    │   └── ApplicationException.java        # 500 - Erros inesperados/sistemas
    ├── external/                            # Integrações externas/comunicação sistemas
    ├── interceptors/                        # Interceptors HTTP, middleware
    └── logging/                             # Configure loggers customizados
```

## 🏗️ Arquitetaura

O projeto segue a arquitetura **hexagonal** com camadas bem definidas:

```
### Camadas Entrypoint & Application
| Camada | Responsabilidade |
|--------|------------------|
| **Entrypoint** | REST Controllers (AutorizacaoController), DTOs requisição/resposta, validação de entrada |
| **Application** | Serviços orquestradores (ContratacaoOrquestradorService), casos de uso PixAutoAutorizacaoService, mapeamentos Mapper |

### Arquitetura Hexagonal
```
┌─────────────────────────────────────┐
│       ENTRYPOINT (REST)              │ ├── Controller
│  ┌───────────────────────────────┐   │ ├── DTOs Request/Response
│  │    APPLICATION (Services)      │   │ ├── Validation Rules
│  ├───────────────────────────────┤   │
│  │      DOMAIN (Entities)         │   │
│  │     ┌──────────────────┐       │   │
│  │     │ Entities:        │       │   │
│  │     │ Authorizacao     │──┐    │   │
│  │     │ Cancelamento     │  \    │   │
│  │     └──────────────────┘   ┌─┘   │   │
│  │                             │      │   │
│  │     ┌──────────────────┐   ┌─┴───┤   │
│  │     │ Enums:           │<──┘     │   │
│  │     │ TipoProduto      │         │   │
│  │     │ StatusAutorizacao│         │   │
│  │     └──────────────────┘         │   │
│  ├───────────────────────────────┤   │
│  │       APPLICATION            │   │
│  │  ┌──────────────────────┐    │   │
│  │  │ PixAutoAutorizacao   │    │   │
│  │  │ Service              │    │   │
│  │  └──────────────────────┘    │   │
│  │                              │   │
│  │  ┌──────────────────────┐    │   │
│  │  │ ContratacaoService   │    │   │
│  │  │ (Strategy Interface) │    │   │
│  │  └──────────────────────┘    │   │
│  ├───────────────────────────────┤   │
│  │      DOMAIN                │   │
│  │     ┌──────────────────┐    │   │
│  │  ───│ Utilities        │    │   │
│  │  ───│ ReversibleUUIDv7 │──┐   │   │
│  │  ───│ PartitionDistributor│\ │   │   │
│  │     └──────────────────┘   \─┤   │   │
│  ├───────────────────────────────┘   │   │
│  │      SHARED (Exceptions, Logging)│   │
│  └──────────────────────────────────┘   │
```

### Entidades & Enums Principais
| Entidade/Enum | Pacote | Descrição |
|---------------|--------|-----------|
| `Autorizacao` | domain/entities | Entidade principal com composite PK (UUID + partição) |
| `Cancelamento` | domain/entities | Entidade para registro de cancelamentos |
| `TipoProduto` | domain/enums/Tipos/tipoEnum | Enum: PIX_AUTO, CARTAO_CREDITO |
| `StatusAutorizacao` | domain/enums/StatusAutorizacao | Enum: ATIVO, CANCELADO, EXPIRADO |
| `MotivoStatusAutorizacao` | domain/enums/Motivos/motivoStatusEnum | Motivos de transição de status |

## 🚀 Como Executar

### Pré-requisitos

- **Java** `25` (JDK 25+) com preview features habilitados
- **Maven** 3.9+ (wrapper pode ser necessário)
- **PostgreSQL** `16+` ou superior com extensões:
  - **pg_partman** - Particionamento automático de tabelas
  - **pg_cron** - Agendamento de tarefas em background

### Variáveis de Ambiente

Configure as seguintes variáveis antes de executar:

```bash
export DB_NAME=contratocommand                # Nome do banco de dados
export DB_USER_NAME=postgres                  # Usuário PostgreSQL  
export DB_PASSWORD=sua_senha_ segura          # Senha PostgreSQL
export SPRING_PROFILES_ACTIVE=dev             # Perfil: dev, prod, integ
```

Ou crie um arquivo `.env` na raiz do projeto com variáveis correspondentes.

### Build Local

```bash
# Clean + compile (executa testes)
mvn clean install -DskipTests=false

# Build sem rodar testes
mvn clean package -DskipTests=true

# Gerar JAR compilado  
mvn clean verify
```

### Executar Aplicação

**Via Maven:**
```bash
mvn spring-boot:run
```

**Via JAR:**
```bash
java -jar target/contratocommand-0.0.1-SNAPSHOT.jar
```
ou
```bash
java --add-modules=jdk.incubator.vector --add-opens=java.base/java.util=ALL-UNNAMED \
   --add-opens=java.base/java.lang=ALL-UNNAMED -jar target/contratocommand.jar
```

A aplicação estará disponível em: `http://localhost:8080`

### PostgreSQL via Docker

Use o Docker Compose fornecido para setup automatico com extensões pg_partman + pg_cron:

```bash
# Build + run imagem completa
docker build -t contratocommand -f Dockerfile.test -f Dockerfile.run_postgres16_ja_com_cron_partman/ .
docker images
docker run -d --name=contratocomcommand \
   -e DB_NAME=contratocommand \
   -e DB_USER_NAME=postgres \
   -e DB_PASSWORD=minha_senha_segura \
   -p 5432:5432 \
   myimage:test

# Acessar console PostgreSQL
docker exec -it contratocomcommand psql -U postgres -d contratocommand
```


## 📚 API Endpoints

### Autorização

| Método | Endpoint | Descrição | Payload | Resposta |
|--------|----------|-----------|---------|----------|
| **GET** | `/api/autorizacao/olaMundo` | Teste de conexão | - | *String* `"Olá, mundo!"` |
| **GET** | `/api/autorizacao/ativas` | Listar autorizações ativas | - | `List<AutorizacaoCompletaResponseDto>` |
| **POST** | `/api/autorizacao` | Criar nova autorização | `CriarAutorizacaoRequest` | `AutorizacaoCreatedResponse` (201) |
| **PATCH** | `/api/autorizacao/{id}/cancelar` | Cancelar autorização | `CancelarAutorizacaoRequest` | `AutorizacaoCompletaResponseDto` |

#### Criar Autorização - Payload

```json
{
  "dataFimVigencia": "2026-12-31",
  "tipoProduto": "PIX_AUTO",
  "valor": 500.00,
  "idAutorizacaoEmpresa": "EMP001",
  "valorLimite": 10000.00,
  "frequencia": 2,
  "quantidadeDividasCiclo": 5,
  "indicadorUsoLimiteConta": 1,
  "codigoCanalContratacao": "01",
  "descricao": "Descrição do contrato",
  "idUnicoContaContratante": "uuid-da-conta",
  "idPessoaPagadora": "uuid-pagador",
  "idPessoaDevedora": "uuid-Devedor",
  "idPessoaRecebedora": "uuid-recebedor"
}
```

#### Campo `frequencia`
- **Validação**: valor entre 1 e 4 (semanal/quincenal/mensal/trimestral)
- **Obrigatório** conforme `@Min(1)` e `@Max(4)`

#### Cancelamento - Payload (`CancelarAutorizacaoRequest`)
- Campo com data de fim da vigência para cancelamento programado
- Processa via Strategy Pattern (PIX Auto, Cartão Crédito, etc)

## 📝 Alterações Recentes - v0.0.1 (abril 2026)

### ✅ API Completa com Strategy Pattern

**Endpoints REST implementados:**
- `GET /api/autorizacao/{id}/cancelar` - Cancelamento via PATCH com Strategy Pattern
- `POST /api/autorizacao` - Criação com orquestrador multi-produto  
- `GET /api/autorizacao/ativas` - Listagem de autorizações ativas com metadados JSON

### ✅ DTOs com Records Imutáveis

**Records Java para DTOs:**

```java
// CriarAutorizacaoRequest - Record imutável com anotações de validação
public record CriarAutorizacaoRequest(
    LocalDate dataFimVigencia,
    @NotNull TipoProduto tipoProduto,
    @NotNull BigDecimal valor,
    @NotNull String idAutorizacaoEmpresa,
    BigDecimal valorLimite,              // Nullable (permiti opcional)
    @Min(1) @Max(4) Integer frequencia,  // Validado: 1-4
    @Min(1) Integer quantidadeDividasCiclo, 
    @NotNull Integer indicadorUsoLimiteConta,
    @NotNull String codigoCanalContratacao,
    String descricao,
    @NotNull UUID idUnicoContaContratante,
    @NotNull UUID idPessoaPagadora,
    @NotNull UUID idPessoaDevedora,
    @NotNull UUID idPessoaRecebedora,
    JsonNode metadados                   // JSONB no PostgreSQL
) {}
```

**Validações:**
- `@Min(1)` em `quantidadeDividasCiclo` - Negará se campo ≤ 0 (422 Unprocessable)  
- `@Max(4)` em `frequencia` - Limita valores válidos entre 1-4

### ✅ Campo Frequencia Validado

```java
// Validação declarativa no DTO:
@Min(value = 1, message = "O campo 'frequencia' deve ser maior ou igual a 1.") 
@Max(value = 4, message = "O campo 'frequencia' deve ser menor ou igual a 4.")
Integer frequencia;
```

Resulta em HTTP 400 Bad Request se violado.

### ✅ DTO de Resposta Completo (`AutorizacaoCompletaResponseDto`)

Conversão automática da entidade para DTO via método `from(Autorizacao)`:

```java
// Builder pattern no response DTO
@Builder @Getter 
public record AutorizacaoCompletaResponseDto(
    IdAutorizacao idAutorizacao,            // Composite PK: UUID + partição
    String typeProd,                         // Normalizado de enum TipoProduto
    LocalDate dataFimVigencia,
    BigDecimal valor,
    String idAutorizacaoEmpresa,
    // ... demais campos da entidade
) {}
```

**Funcionalidades:**
- Parse automático do `JsonNode metadados` (JSONB) para representação JSON válida  
- Converte `TipoProduto` enum → string normalizada (`"PIX_AUTO"` vs `"pix_auto"`)
- Mapeamento de enums para formatos legíveis no payload REST response

### ✅ Composite Primary Key Strategy Pattern

**Chave composta implementada em `IdAutorizacao`:**

```java
record IdAutorizacao(
    UUID idAutos,      // UUID ReversibleUUIDv7 com partição embutida  
    Integer idParticaoConta // Identificador de partição temporal (1-100)
)
```

**Pattern de estratégia via interface:**
- `ContratacaoService` como contrato de Strategy Pattern
- Factory `ProdutoStrategyFactory` seleciona estratégia por `tipoProduto`  
- Permite estender para novo produto sem modificar código existente (`Open/Closed Principle`)

### ✅ Mapeamento Automático com `@AfterMapping`

**MapStruct implementa conversão:**

```java
@Mapper(componentModel = "spring")
public interface PixAutoAutorizacaoMapper {
    @Mapping(target = "id", ignore = true)  // Ignora primary key no mapeamento
    
    Autorizacao toEntity(CriarAutorizacaoRequest request);
    
    @AfterMapping
    default void afterToEntity(CriarAutorizacaoRequest request, 
                                @MappingTarget Autorizacao autorizacao) {
        // Lógica customizada após mapeamento automático
        
        // Exemplo: Define composite PK + configura status inicial
        IdContaUUIDPartitionDistributor distributor = new IdContaUUIDPartitionDistributor();
        String uuidComParticao = ReversibleUUIDv7.generate(
            distribuidor.obterParticaoIdConta()
        );
        
        autorizacao.setId(new IdAutorizacao(uuidComParticao));
        autorizacao.setStatus(Status Autorizacao.ATIVO);
    }
}
```

### ✅ Validadores Customizados (`ContratacaoValidator`)

**Pipeline de validação em cascata:**

| Fase | Componente | Responsabilidade | Exceção | Status HTTP |
|------|-----------|------------------|---------|------------|
| 1 | `@Valid` nos DTOs | Captura erros básicos (@NotNull, @Min) | `BindException` | 400 Bad Request |
| 2 | Validadores customizados | Regras específicas (campo mínimo necessário) | `BusinessException` | 422 Unprocessable |
| 3 | Service layer | Validações de negócio internas | `BusinessException` | 422 |

**Implementação:**
- `CampoMinimoNecessarioValidator.java` - Negara requisições com campos vazios  
- `DataFimVigenciaInvalida.java` - Verifica intervalo temporal válido para data fim  
- Centraliza lógica customizada fora de validações declarativas padrão JSR-380

### ✅ Tratamento de Exceções (`ApiExceptionHandler`)

**Handler centralizado define mapeamento:**

## ⚙️ Configurações

O arquivo `application.yaml` contém configurações mínimas:

```yaml
spring:
  application:
    name: contratocommand
  datasource:
    url: jdbc:postgresql://localhost:5432/${DB_NAME}
    username: ${DB_USER_NAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update     # Opções: validate, update, create, create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect  # PostgreSQL específico
```

### DDL Auto - Comportamento da Tabela

| Valor | Ação no Schema Hibernate | Recomendação |
|-------|-------------------------|--------------|
| **validate** | Valida schema existente sem alterações | Dev/Prod (schema precriado) |
| **update** | Altera tabelas conforme mudanças do entity | Dev/Testes locais |
| **create** | Cria schema do zero, apaga dados existentes | Reset completo |
| **create-drop** | Cria e destrói ao final da aplicação | Testing |

### Particionamento Temporal (`pg_partman`)

A tabela `autorizacoes` utiliza particionamento por intervalo temporal:

```sql
-- Estrutura de composite primary key
PRIMARY KEY (id_autorizacao, id_particao_conta)
  ├── id_autorizacao: UUID aleatório (ReversibleUUIDv7)
  └── id_particao_conta: INTEGER (1-100) -- Identifica partição temporal

-- Para obter partição de escrita ATUAL:
SELECT ControleExpurgoAutorizacao.obterParticaoExpurgoWrite()::INTEGER;

-- Para dropar partição ANTIGA:
SELECT ControleExpurgoAutorizacao.obterParticaoExpurgoDrop()::INTEGER;
```

**UUIDS Reversíveis (ReversibleUUIDv7):**
- Gerados com timestamp+sequencial, incluem metadata de partição embutida
- Extração: `ReversibleUUIDv7.extract(uuid)` → retorna `id_particao_conta` como INTEGER
- Substitui UUID puro aleatório para permitir particionamento temporal sem query adicional

**⚠️ CRÍTICO:** Nunca use UUIDs random puros em operações de escritura; toda nova `Autorizacao` passa por:
1. `IdContaUUIDPartitionDistributor.getPartitionFast()` → obtém INTEGER partição atual
2. `ReversibleUUIDv7.generate(particao)` → gera UUID com partição embutida


## 🧪 Testes

```bash
# Executar todos os testes
mvn test

# Executar com cobertura
mvn clean test jacoco:report
```

A classe [ContratocommandApplicationTests.java](src/test/java/br/com/srportto/contratocommand/ContratocommandApplicationTests.java) contém testes básicos da aplicação.

## 📦 Dependências Principais

```xml
<!-- Framework Web -->
spring-boot-starter-webmvc
spring-boot-starter-validation
spring-boot-starter-test

<!-- Persistência -->
spring-boot-starter-data-jpa
postgresql

<!-- Utilitários -->
lombok (1.18.40)
mapstruct (1.5.5.Final)
yasson (3.0.3)
```

## 🔧 Padrões de Design Utilizados

| Padrão | Aplicação no Projeto |
|--------|---------------------|
| **Strategy Pattern** | Interface `ContratacaoService` + Factory para multiplidade produtos (PIX, Cartão) |
| **Composite Primary Key** | `(UUID idAutos, Integer particao)` em queries JPA com distribuições customizadas via repo |
| **Open/Closed Principle** | Adicionar novo produto sem modificar código: implementar estratégia + registrar no factory |

## 📝 Convenções de Codificação

### Nomenclatura
- **Entidades**: Substantivo singular (`Autorizacao`, `Cancelamento`)
- **Serviços**: `{Substantivo}Service` ou `{Operacao}Service`  
- **Repositories**: `{Substantivo}Repository` (JPA) ou `*Distributor` para utilitários de domínio
- **Mappers**: `{Entidade}Mapper` (MapStruct)
- **Controllers**: `{RecursosRestrita}Controller` no entrypoint package
- **DTOs Requisição**: `{Operacao}{Substantivo}Request` (ex: `CriarAutorizacaoRequest`)
- **DTOs Resposta**: `{Substantivo}Response` ou `*ResponseDto` com Lombok

### Pacotes da Arquitetura Hexagonal

| Camada | Package | Responsabilidade |
|--------|---------|------------------|
| Application | `application` | Orquestração de casos de uso (Services, Mappers, DTOs) |
| Domain | `domain/` | Lógica pura: entidades, enums, modelos, utils de negócio |
| Entrypoint | `entrypoint/` | Inbound adapters (Controllers), DTOs externos, interceptores |  
| Shared | `shared/` | Configurações globais, exceções customizadas, utilitários compartilhados |

## 📖 Documentação Externa

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)  
- [MapStruct Documentation](https://mapstruct.org/)
- [Jakarta Bean Validation 3.0](https://jakarta.ee/specifications/validate/)
- [PostgreSQL Partitioned Tables](https://www.postgresql.org/docs/current/ddl-partitioning.html)
- [pg_partman Extension](https://github.com/pgpartman/pg_partman)

## 🤝 Contribuindo

### Workflow Padrão

1. **Fork** do repositório principal
2. **Clone local**: `git clone https://github.com/your-username/contratocommand.git`
3. **Crie branch feature**: `git checkout -b feature/nova-funcionalidade`  
4. **Desenvolva + commit**: `git commit -m 'Implementa {nova_feature}'`  
5. **Push para remota**: `git push origin feature/{nome-feature}`  
6. Abra um Pull Request no repositório original

### Diretrizes de Código

- Segue arquitetura hexagonal: Domain layer independente de frameworks
- DTOs imutáveis com records quando possível
- Validadores customizados em regras específicas, não espalhados
- Composite PK `(UUID, Integer)` apenas no contexto particionamento temporal
- Exceptions mapeadas para status HTTP apropriado (400/422/500)

## 📄 Licença e Código Aberto

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](./LICENSE) para detalhes.

Utiliza bibliotecas de código aberto conforme especificado em `pom.xml`.

## 👨‍💻 Desenvolvedor

**Organização**: SR Porto  
**Grupo**: br.com.srportto  
**Versão**: 0.0.1-SNAPSHOT  
**Java Version**: 25 (JDK 25+ com preview features)  
**Última atualização**: 29 de abril de 2026

## 📞 Suporte

Para dúvidas ou problemas, abra uma issue no repositório ou entre em contato com a equipe de desenvolvimento.

---

## ⚠️ Notas Importantes Sobre Java 25 Preview Features

Este projeto utiliza preview features do JDK 25 que podem exigir flags JVM específicas:

```bash
# Para compilar e executar com Java 25
java --version
javac --verbose --source 25 --release 25 \
   -Xlint:none --add-modules=jdk.incubator.vector --add-opens=java.base/java.util=ALL-UNNAMED \
   -jar target/contratocommand.jar
```

**Se encontrar erros de compilação**, verifique:
1. JDK 25 instalado e configurado (`JAVA_HOME` apontando para JDK 25)
2. Maven com plugin compatible Java 25+  
3. Preview features habilitados no build (se necessário)

---

📌 **Documentação Completa**: Arquivos em `docs/` contêm detalhes técnicos sobre particionamento, payloads de testes e scripts SQL para setup de banco de dados.
