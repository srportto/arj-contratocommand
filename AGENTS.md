# 🤖 Instruções para Agentes de IA — arj-contratocommand

Uma API REST de **autorizações PIX automáticas** com arquitetura hexagonal, particionamento temporal de dados e expurgo automático.

## 🎯 Comece Por Aqui

Leia **nesta ordem**:
1. [README.md](README.md) — visão geral do projeto
2. [PixAutoAutorizacaoService.java](src/main/java/br/com/srportto/contratocommand/application/pixauto/PixAutoAutorizacaoService.java) — orquestração de negócio (90% da lógica)
3. [AutorizacaoController.java](src/main/java/br/com/srportto/contratocommand/entrypoint/AutorizacaoController.java) — endpoints REST
4. [Autorizacao.java](src/main/java/br/com/srportto/contratocommand/domain/model/Autorizacao.java) — entidade de domínio com particionamento

## 🏗️ Arquitetura & Camadas

```
domain/          → Entidades (Autorizacao, Cancelamento), Enums, Utilities (lógica pura)
application/     → Serviços orquestradores, Mappers, Repositórios
entrypoint/      → Controllers REST, DTOs
shared/          → Exceções (BusinessException), Interceptadores, Configs
```

**Padrão hexagonal**: Domínio isolado, interfaces de porta, adaptadores de entrada/saída.

### Nomeclatura & Convenções

- **Serviços**: `*Service` (ex: `PixAutoAutorizacaoService`)
- **Repositórios**: `*Repository` estendendo `JpaRepository`
- **Mappers**: `*Mapper` com `@Mapper(componentModel = "spring")` (MapStruct)
- **Controladores**: `*Controller` com `@RestController`
- **Exceções de negócio**: `throw new BusinessException("mensagem")`
- **DTOs com records**: Imutáveis; recriar para modificar valores

## 🔧 Build & Testes

```bash
# Build & testes
mvn clean package                          # Compilar + rodar testes
mvn test -Dtest=ClasseTest                # Testar classe específica
mvn test -Dtest=ClasseTest#metodoTest     # Testar método específico
mvn spring-boot:run                        # Rodar aplicação localmente

# Limpar (Maven wrapper quebrado? Use `mvn` diretamente)
mvn clean
```

**⚠️ Maven Wrapper Issue**: Se `./mvnw.cmd` falhar com erro PowerShell, use `mvn` diretamente em vez disso.

**Testes**: JUnit 5 via `spring-boot-starter-test`. Testes de integração exigem PostgreSQL rodando.

## ⚙️ Stack Técnico

| Componente | Versão | Notas |
|---|---|---|
| **Java** | 25 | Novo `void main()` em vez de `public static void main()` |
| **Spring Boot** | 4.0.4 | Web MVC, Data JPA, Validation |
| **Lombok** | 1.18.40 | Reduz boilerplate (`@Data`, `@Getter`, `@Setter`) |
| **MapStruct** | 1.5.5.Final | Mapeamento DTO↔Entity automático |
| **PostgreSQL** | 16+ | Particionamento com `pg_partman` + `pg_cron` |

## 🚨 Armadilhas Críticas

### 1. **Particionamento de Dados**
- Tabela `autorizacoes` particionada por `id_particao_conta` (1-100)
- Chave composta: `(id_autorizacao, id_particao_conta)`
- **Para criar**: Use `IdContaUUIDPartitionDistributor.getPartitionFast()` ou `ControleExpurgoAutorizacao.obterParticaoExpurgoWrite()`
- **Para dropar**: Use `ControleExpurgoAutorizacao.obterParticaoExpurgoDrop()` (valida se partição não conflita com escrita atual)

### 2. **UUIDs Especiais (ReversibleUUIDv7)**
- Gerados com partição embutida para extrair depois: `ReversibleUUIDv7.extract(uuid)`
- Nunca use UUID aleatório puro; sempre coordene com `IdContaUUIDPartitionDistributor`

### 3. **DTOs com Records são Imutáveis**
```java
// ❌ Errado: tentar modificar record
CriarAutorizacaoRequest req = ...;
req.valor = 5000;  // Compile error!

// ✅ Certo: recriar com novo valor
CriarAutorizacaoRequest novaReq = new CriarAutorizacaoRequest(..., 5000, ...);
```

### 4. **PostgreSQL Obrigatório**
- Não há fallback para H2 ou outras databases — Hibernate dialect é PostgreSQL específico
- Docker-compose fornecido: `run_postgres16_ja_com_cron_partman/`
- Variáveis de ambiente obrigatórias: `DB_NAME`, `DB_USER_NAME`, `DB_PASSWORD`

### 5. **Validações em Cascata**
- `@Valid` no DTO captura erros de input (400 Bad Request)
- `BusinessException` é lançada para violações de regra de negócio (422 Unprocessable Content)
- `ApplicationException` é lançada para erros inesperados (500 Internal Server Error)

### 6. **Java 25 Preview Features**
- Uso de `void main()` ao invés de `public static void main()`
- Instale JDK 25+; versões antigas não compilam

## 📚 Documentação & Recursos

**Em `docs/`**:
- [info_build-my-image-and-execute.md](docs/info_build-my-image-and-execute.md) — Docker & PostgreSQL com partman + cron
- [comandos-sql.txt](docs/comandos-sql.txt) — Scripts SQL para particionamento
- [post-autorizacoes.txt](docs/post-autorizacoes.txt) — Exemplos de payloads REST
- [detalhes-particonamento.drawio](docs/detalhes-particonamento.drawio) — Diagrama de particionamento

**Arquivos-chave de código**:
- `domain/utilities/ControleExpurgoAutorizacao.java` — Cálculo de partições (escrita vs drop)
- `domain/utilities/IdContaUUIDPartitionDistributor.java` — Distribuição de UUIDs com partição
- `application/pixauto/PixAutoAutorizacaoMapper.java` — Callbacks do MapStruct para lógica customizada

## 🎓 Padrões Recorrentes

### Validação com BusinessException
```java
if (dataFim.isBefore(LocalDate.now())) {
    throw new BusinessException("Data de fim não pode estar no passado");
}
```

### Mapeamento com MapStruct + AfterMapping
```java
@Mapper(componentModel = "spring")
public interface PixAutoAutorizacaoMapper {
    @Mapping(target = "id", ignore = true)
    Autorizacao toEntity(CriarAutorizacaoRequest dto);
    
    @AfterMapping
    default void afterToEntity(CriarAutorizacaoRequest dto, @MappingTarget Autorizacao autorizacao) {
        // Lógica customizada pós-mapeamento
    }
}
```

### Transações Explícitas
```java
@Service
public class PixAutoAutorizacaoService {
    @Transactional
    public void criar(...) {
        // Lógica dentro da transação
    }
}
```

## ✅ Checklist Para Antes de Commit

- [ ] Testes passando: `mvn test`
- [ ] Sem erros de compilação: `mvn clean compile`
- [ ] Sem warnings de log: verificar `[WARN]` no console
- [ ] Exceções apropriadas: `BusinessException` para regras, `ApplicationException` para erros inesperados
- [ ] Partições validadas: Se mexer em `ControleExpurgoAutorizacao` ou `IdContaUUIDPartitionDistributor`, testar `ControleExpurgoAutorizacaoTest`
- [ ] DTOs imutáveis: Não tentar modificar records; recriar quando necessário

---

**Última atualização**: 20 de abril de 2026

Para sugestões ou melhorias neste arquivo, abra uma issue ou faça um comentário no PR.
