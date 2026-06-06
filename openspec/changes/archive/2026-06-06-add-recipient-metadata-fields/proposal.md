## Why

Atualmente, a rota de criação de autorização não captura dados detalhados sobre o beneficiário/recebedor. Estruturar esses dados em um objeto de metadado com campos específicos (`nomePessoaRecebedora` e `apelidoPessoaRecebedora`) melhora a rastreabilidade, auditoria e experiência do usuário ao consultar autorizações. Isso permite que clientes enriqueçam o registro de autorização com informações contextuais do beneficiário no momento da criação.

## What Changes

- Adiciona objeto `metadado` ao DTO `CriarAutorizacaoRequest` contendo campos `nomePessoaRecebedora` (string) e `apelidoPessoaRecebedora` (string)
- Campo `metadado` é opcional e pode ser omitido no request
- Persiste o objeto `metadado` completo na tabela `autorizacoes`
- Retorna o objeto `metadado` na resposta de listagem GET `/api/autorizacoes/listar`
- Valida comprimento máximo e formato dos campos dentro do metadado

## Capabilities

### New Capabilities
- `recipient-metadata-structure`: Captura, validação, persistência e retorno de dados de beneficiário/recebedor estruturados em objeto `metadado` com campos `nomePessoaRecebedora` e `apelidoPessoaRecebedora`

### Modified Capabilities
<!-- Existing capabilities whose REQUIREMENTS are changing (not just implementation).
     Only list here if spec-level behavior changes. Each needs a delta spec file.
     Use existing spec names from openspec/specs/. Leave empty if no requirement changes. -->

## Impact

- **DTO de Entrada**: `CriarAutorizacaoRequest` - adição de campo `metadado` com sub-estrutura
- **DTO de Saída**: `ListarAutorizacoesResponse` - inclusão de campo `metadado` na resposta
- **Entidade de Domínio**: `Autorizacao` - novo campo para armazenar estrutura de metadado
- **Banco de Dados**: coluna `metadado` adicionada (tipo JSON/JSONB) à tabela `autorizacoes`
- **Mapper MapStruct**: `PixAutoMapper` e `DdaAutoMapper` precisam mapear o novo campo
- **Validação**: Regras de validação para comprimento e conteúdo dos campos de metadado
- **API**: Contratos POST e GET expandidos para incluir o novo objeto de metadado
