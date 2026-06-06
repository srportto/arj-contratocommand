## Contexto

O sistema de autorização usa arquitetura hexagonal com quatro camadas:
- **Entrypoint** (`AutorizacaoController`): API REST e DTOs (records imutáveis)
- **Application** (`ContratacaoOrquestradorService`, Use Cases, Mappers, Repositories)
- **Domain** (entidade `Autorizacao`, regras de validação, lógica de negócio)
- **Shared** (exceções, interceptadores, framework de validação)

Atualmente, o DTO `CriarAutorizacaoRequest` e a entidade `Autorizacao` não possuem campo para armazenar dados estruturados do beneficiário/recebedor. O campo `metadado` com sub-estrutura JSON precisa fluir através de todas as quatro camadas seguindo os padrões estabelecidos.

**Consideração de particionamento**: A tabela `autorizacoes` usa chaves primárias particionadas por range com `idParticaoConta`. O campo `metadado` será adicionado como coluna JSONB e não afeta a lógica de particionamento.

## Objetivos / Fora do Escopo

**Objetivos:**
- Aceitar objeto `metadado` (opcional) em POST `/api/autorizacoes` contendo `nomePessoaRecebedora` e `apelidoPessoaRecebedora`
- Persistir o metadado completo em coluna JSONB na tabela `autorizacoes`
- Retornar o metadado em GET `/api/autorizacoes/listar`
- Validar comprimento e conteúdo dos campos dentro do metadado
- Suportar produtos PixAuto e DdaAuto (padrão strategy se aplica)

**Fora do Escopo:**
- Adicionar metadado ao endpoint PATCH `/api/autorizacoes/{id}/cancelar`
- Implementar busca full-text ou indexação no nome do recebedor
- Alterar esquema de chave primária ou particionamento
- Criar novas regras de autorização baseadas em validação de metadado

## Decisões

### Decisão 1: Campo metadado é opcional com validação de comprimento
**Rationale**: Nem todas as autorizações exigem dados do beneficiário registrados no momento da criação. Tornar opcional preserva compatibilidade com clientes existentes e evita quebra de contrato de API. Validação de comprimento máximo (255 chars por campo) alinha com práticas de banco de dados e REST.

**Alternativas consideradas**:
- Tornar metadado obrigatório → quebra contratos de API existentes, requer migração de clientes
- Tornar obrigatório apenas para produtos específicos → adiciona complexidade à lógica de validação por produto

### Decisão 2: Não usar tipo JSONB no PostgreSQL para armazenar metadado
**Rationale**: Usar schema pré existente em JSON(simples), com objetivo de não realizar nenhuma migration no banco e minimizar riscos 

**Alternativas consideradas**:
- Usar coluna TEXT com JSON serializado → sem validação native, difícil para queries
- Criar tabelas separadas para nome e apelido → over-engineering, mais joins nas queries
- Alterar a coluna para JSONB , risco de dawntime durante execução da migration no banco de dados

### Decisão 3: Usar classe wrapper `Metadado` e MapStruct `@AfterMapping`
**Rationale**: Cria tipo de domínio explícito para estrutura de metadado, facilitando validação e reutilização. Consistente com padrão existente de mappers que usam `@AfterMapping` para campos derivados e inicialização.

**Alternativas consideradas**:
- Armazenar direto como Map/Object na entidade → tipos fracos, difícil para validação e testes
- Usar Jackson annotations no DTO → mistura concerns de serialização com lógica de domínio

### Decisão 4: Validar metadado em `ContratacaoValidator` com `MetadadoRule`
**Rationale**: O sistema já tem framework de `ContratacaoValidator` que injeta `List<ContratacaoRule>`. Adicionar `MetadadoRule` reutiliza padrão de validação e mantém centralizado. Regra específica verifica comprimento e caracteres permitidos.

**Alternativas consideradas**:
- Usar `@NotBlank`, `@Size` do JSR-380 → insuficiente para regras complexas (caracteres proibidos), difícil de testar em isolamento
- Validar no Use Case → acopla validação com lógica de negócio

### Decisão 5: Incluir metadado em resposta de listagem desde o início
**Rationale**: Garante que o campo é útil e testável imediatamente após deploy. Se não retornado, ninguém sabe que está lá. Validar retorno é parte do contrato de API.

**Alternativas consideradas**:
- Retornar metadado apenas sob flag feature → adiciona complexidade condicional desnecessária
- Não retornar inicialmente → risco de descobrir problemas na desserialização depois

## Riscos / Trade-offs

| Risco | Mitigação                                                                                    |
|-------|----------------------------------------------------------------------------------------------|
| Migração em tabela `autorizacoes` grande impacta downtime | Não será realizada a migration para mudar a coluna metadado para JSONB                       |
| Estrutura de metadado pode evoluir, exigindo versionamento | Esse risco nao impacta o negocio, nao versionar                                              |
| Validação muito restrita rejeita inputs válidos | Definir charset claro (alfanumérico, espaços, hífens, etc.) antes de implementação           |
| Desserialização JSON falha em dados malformados | Usar `@JsonDeserialize` customizado que lança `BusinessException` legível, não erro técnico  |
| Performance de queries com JSON| Não será usada nenhum querie sobre esse JSON , nao criar índice GIN para a coluna `metadado` |

## Plano de Migração

1. **Implementação de código**:
   - Adicionar classe `Metadado` record com campos `nomePessoaRecebedora` e `apelidoPessoaRecebedora`
   - Adicionar campo `metadado: Metadado` a `CriarAutorizacaoRequest` DTO
   - Adicionar campo `metadado: Metadado` a entidade `Autorizacao`
   - Adicionar mapeamento em `PixAutoMapper` e `DdaAutoMapper` via `@Mapping` ou `@AfterMapping`
   - Criar `MetadadoRule` component com validação de campos
   - Injetar `MetadadoRule` automaticamente em `ContratacaoValidator`
   - Adicionar `metadado` a DTO de resposta `ListarAutorizacoesResponse`

2. **Validação**:
   - Confirmar GET retorna campo corretamente para registros com/sem metadado
   - Verificar POST rejeita metadado com campos > 255 caracteres com HTTP 422
   - Testar com produtos PixAuto e DdaAuto
   - Validar que POST sem metadado ainda funciona (backward compatibility)

3. **Rollback** (se necessário):
   - Remover campo de DTO e entidade
   - Reverter migração de database (requer downtime)

## Questões em Aberto

1. Quais são as restrições de caracteres para os campos (apenas ASCII, aceita acentuação)? remover acentuação
2. Existe comprimento máximo recomendado menor que 255 caracteres? Não
3. O campo `metadado` deve ser indexado para buscas futuras (e.g., filtrar por `nomePessoaRecebedora`)? Não
4. Sistemas de auditoria/logging precisam capturar mudanças ao campo `metadado`?Não
5. Há casos de uso para atualizar o metadado após criação, ou é imutável após criação? é imutavel
