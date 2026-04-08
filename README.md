# Contrato Command

Um microserviço robusto desenvolvido com arquitetura limpa e Spring Boot, responsável pelo gerenciamento de autorizações e processamento de contratos, com foco em operações PIX automáticas.

## 📋 Sobre o Projeto

O **Contrato Command** é uma aplicação backend construída seguindo os princípios de Domain-Driven Design (DDD) e arquitetura hexagonal. Ele fornece APIs REST para gerenciar autorizações de contratos, suportando operações de autorização, cancelamento e consulta de transações PIX automáticas.

### Funcionalidades Principais

- ✅ Autenticação e autorização de contratos
- ✅ Processamento de transações PIX automáticas
- ✅ Gerenciamento de cancelamentos
- ✅ Persistência em banco de dados PostgreSQL
- ✅ Validação robusta de dados
- ✅ Tratamento centralizado de exceções
- ✅ Logging estruturado

## 🛠️ Tecnologias

### Core Framework
- **Spring Boot** `4.0.4` - Framework web e IoC
- **Spring Data JPA** - Persistência de dados e ORM
- **Spring Validation** - Validação de entrada de dados
- **Spring Web MVC** - Construção de APIs REST

### Linguagem & Compilação
- **Java** `25` - Linguagem de programação compilada
- **Maven** - Gerenciamento de dependências e build
- **Lombok** `1.18.40` - Redução de boilerplate através de anotações

### Padrões & Mapeamento
- **MapStruct** `1.5.5.Final` - Mapeamento automático entre DTOs e entidades
- **Jakarta Validation** - Validação de dados com suporte a custom validators

### Banco de Dados
- **PostgreSQL** - Banco de dados relacional
- **Hibernate** - Dialeto PostgreSQL para JPA

### Serialização
- **Yasson** `3.0.3` - Serialização/desserialização JSON (Jakarta JSON Binding)

## 📁 Estrutura do Projeto

```
src/main/java/br/com/srportto/contratocommand/
├── ContratocommandApplication.java          # Classe principal da aplicação
│
├── application/                             # Camada de aplicação
│   └── pixauto/                             # Caso de uso PIX automáticas
│       ├── PixAutoAutorizacaoService.java   # Orquestração de negócio
│       ├── PixAutoAutorizacaoRepository.java # Interface de dados
│       └── PixAutoAutorizacaoMapper.java    # Mapeamento DTO <-> Entity
│
├── domain/                                  # Camada de domínio
│   ├── entities/                            # Entidades de negócio
│   │   ├── Autorizacao.java                 # Entidade de autorização
│   │   └── Cancelamento.java                # Entidade de cancelamento
│   ├── enums/                               # Enumeradores do domínio
│   │   ├── StatusAutorizacao.java           # Estados de autorização
│   │   ├── MotivoStatusAutorizacao.java     # Motivos de mudança de status
│   │   └── CanaisConhecidosEnum.java        # Canais conhecidos
│   └── model/                               # Modelos de domínio
│       └── ContratoBase.java                # Modelo base de contrato
│
├── entrypoint/                              # Camada de entrada (apresentação)
│   ├── AutorizacaoController.java           # Endpoint REST
│   └── contratosrest/                       # Objetos de transferência de dados
│       ├── ReceberAutorizacaoRequest.java   # DTO de requisição
│       └── componentscontrato/              # Componentes específicos
│
└── shared/                                  # Camada compartilhada/infraestrutura
    ├── configurations/                      # Configurações da aplicação
    ├── exceptions/                          # Exceções customizadas
    │   ├── ApplicationException.java        # Exceções de aplicação
    │   └── BusinessException.java           # Exceções de negócio
    ├── external/                            # Integrações externas
    ├── interceptors/                        # Interceptadores HTTP
    │   └── api/                             # Tratamento de exceções REST
    │       ├── ApiExceptionHandler.java     # Handler global de exceções
    │       ├── BodyOcorrenciasErrosValidations.java
    │       ├── LayoutErrosApiResponse.java
    │       └── LayoutErrosApiValidationsResponse.java
    └── logging/                             # Utilitários de logging

src/main/resources/
└── application.yaml                        # Configurações da aplicação
```

## 🏗️ Arquitetura

O projeto segue a arquitetura **hexagonal** com camadas bem definidas:

```
┌─────────────────────────────────────────┐
│      ENTRYPOINT (REST Controllers)       │
├─────────────────────────────────────────┤
│      APPLICATION (Services)              │
├─────────────────────────────────────────┤
│      DOMAIN (Entities, Models)           │
├─────────────────────────────────────────┤
│      SHARED (Config, Exceptions, Utils)  │
└─────────────────────────────────────────┘
```

### Camadas

| Camada | Responsabilidade |
|--------|-----------------|
| **Entrypoint** | Controllers REST, recepção e resposta de requisições HTTP |
| **Application** | Serviços, orquestração de operações, DTOs, mapeamento |
| **Domain** | Entidades, enumeradores, modelos de negócio puros |
| **Shared** | Configurações, exceções, interceptadores, logging |

## 🚀 Como Executar

### Pré-requisitos

- Java 25 ou superior
- Maven 3.6+
- PostgreSQL 12 ou superior

### Variáveis de Ambiente

Configure as seguintes variáveis antes de executar:

```bash
export DB_NAME=contratocommand        # Nome do banco de dados
export DB_USER_NAME=postgres          # Usuário do PostgreSQL
export DB_PASSWORD=sua_senha           # Senha do PostgreSQL
```

Ou crie um arquivo `.env` na raiz do projeto.

### Instalação e Build

```bash
# Clonar o repositório (se aplicável)
git clone <url-do-repositorio>
cd contratocommand

# Compilar e executar testes
mvn clean install

# Gerar apenas os JARs
mvn clean package
```

### Executar a Aplicação

```bash
# Via Maven
mvn spring-boot:run

# Ou via JAR compilado
java -jar target/contratocommand-0.0.1-SNAPSHOT.jar
```

A aplicação estará disponível em: `http://localhost:8080`

## 📚 API Endpoints

### Autorização

- **GET** `/api/autorizacao/olaMundo` - Endpoint de teste
- **GET** `/api/autorizacao/ativas` - Listar autorizações ativas (retorna `AutorizacaoCompletaResponseDto`)
- **POST** `/api/autorizacao` - Criar nova autorização com validação de campo mínimo 1 em `quantidadeDividasCiclo` (retorna `AutorizacaoCompletaResponseDto`)

## 📝 Alterações Recentes

### v0.0.1 - Validações e DTO Completa (04/2026)

#### ✅ Validações Implementadas
- **Campo `quantidadeDividasCiclo`**: Agora nega requisições com valores menores que 1 via `@Min(value = 1)`

#### 📤 Resposta Completa da Entidade
- Criada a classe `AutorizacaoCompletaResponseDto` como DTO simples com Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- **Mapeamento de Metadados**: A coluna `metadados` do banco (tipo `JSONB`) é parseada para `JsonNode` e retornada ao client em formato JSON válido
- Método factory `from(Autorizacao)` para conversão automática com builder pattern
- Endpoints POST e GET retornam representação completa via `AutorizacaoCompletaResponseDto`

## ⚙️ Configurações

O arquivo `application.yaml` contém as configurações da aplicação:

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
      ddl-auto: update     # auto, validate, update, create, create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Hibernated DDL Auto

- `validate` - Valida o schema sem fazer mudanças
- `update` - Atualiza o schema se necessário
- `create` - Cria o schema, destroi dados anteriores
- `create-drop` - Cria e destrói ao desligar

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

- **Domain-Driven Design (DDD)** - Organização por domínios de negócio
- **Repository Pattern** - Abstração de acesso a dados
- **Service Pattern** - Lógica de negócio centralizada
- **Data Transfer Object (DTO)** - Transferência segura de dados
- **Mapper Pattern** - Mapeamento automático entre camadas
- **Exception Handling** - Tratamento centralizado de erros
- **Dependency Injection** - Inversão de controle via Spring

## 📝 Convenções

### Nomenclatura
- Entidades: `Substantivo` (ex: `Autorizacao`, `Cancelamento`)
- Serviços: `{Entidade}Service` (ex: `PixAutoAutorizacaoService`)
- Repositories: `{Entidade}Repository` (ex: `PixAutoAutorizacaoRepository`)
- Mappers: `{Entidade}Mapper` (ex: `PixAutoAutorizacaoMapper`)
- Controllers: `{Recurso}Controller` (ex: `AutorizacaoController`)
- DTOs Requisição: `{Operacao}{Entidade}Request`
- DTOs Resposta: `{Entidade}Response`

### Pacotes
- `application` - Casos de uso e orquestração
- `domain` - Lógica e modelos de domínio
- `entrypoint` - Inbound adapters (Controllers)
- `shared` - Código compartilhado e infraestrutura

## 📖 Documentação Adicional

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [MapStruct Documentation](https://mapstruct.org/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Jakarta Validation](https://jakarta.ee/specifications/validation/)

## 🤝 Contribuindo

1. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
2. Commit suas mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
3. Push para a branch (`git push origin feature/nova-funcionalidade`)
4. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença definida no arquivo LICENSE (a ser preenchido).

## 👨‍💻 Desenvolvedor

- **Organização**: SR Porto
- **Grupo**: br.com.srportto
- **Versão**: 0.0.1-SNAPSHOT
- **Java Version**: 25

## 📞 Suporte

Para dúvidas ou problemas, abra uma issue no repositório ou entre em contato com a equipe de desenvolvimento.

---

**Última atualização**: 6 de abril de 2026
