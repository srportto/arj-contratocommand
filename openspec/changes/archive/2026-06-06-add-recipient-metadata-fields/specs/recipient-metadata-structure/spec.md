## REQUISITOS ADICIONADOS

### Requisito: Aceitar estrutura de metadado com dados do beneficiário na criação

O sistema DEVE aceitar um objeto `metadado` opcional na requisição POST `/api/autorizacoes` contendo os campos `nomePessoaRecebedora` e `apelidoPessoaRecebedora`.

#### Cenário: Metadado com dados do beneficiário fornecido
- **QUANDO** uma requisição POST para `/api/autorizacoes` inclui um objeto `metadado` com `nomePessoaRecebedora` e `apelidoPessoaRecebedora` válidos (máximo 255 caracteres cada)
- **ENTÃO** a autorização é criada com sucesso e o metadado é armazenado

#### Cenário: Metadado omitido na requisição
- **QUANDO** uma requisição POST para `/api/autorizacoes` não inclui o objeto `metadado`
- **ENTÃO** a autorização é criada com sucesso e o campo `metadado` permanece nulo no banco de dados

#### Cenário: Campo nomePessoaRecebedora excede comprimento máximo
- **QUANDO** uma requisição POST inclui `metadado.nomePessoaRecebedora` com mais de 255 caracteres
- **ENTÃO** o sistema rejeita a requisição com HTTP 422 e retorna erro de validação de negócio

#### Cenário: Campo apelidoPessoaRecebedora excede comprimento máximo
- **QUANDO** uma requisição POST inclui `metadado.apelidoPessoaRecebedora` com mais de 255 caracteres
- **ENTÃO** o sistema rejeita a requisição com HTTP 422 e retorna erro de validação de negócio

### Requisito: Persistir metadado na tabela de autorizações

O sistema DEVE armazenar o objeto `metadado` completo na coluna `metadado` da tabela `autorizacoes` em formato JSON/JSONB.

#### Cenário: Metadado é persistido corretamente
- **QUANDO** uma autorização é criada com um objeto `metadado` válido
- **ENTÃO** o valor é armazenado na coluna `metadado` do banco de dados exatamente como recebido

#### Cenário: Metadado nulo é persistido
- **QUANDO** uma autorização é criada sem o objeto `metadado`
- **ENTÃO** a coluna `metadado` armazena um valor nulo (NULL) no banco de dados

### Requisito: Retornar metadado na listagem de autorizações

O sistema DEVE retornar o objeto `metadado` na resposta de GET `/api/autorizacoes/listar`, incluindo os campos `nomePessoaRecebedora` e `apelidoPessoaRecebedora` quando disponíveis.

#### Cenário: Metadado é retornado na listagem
- **QUANDO** uma requisição GET é feita para `/api/autorizacoes/listar`
- **ENTÃO** a resposta inclui o campo `metadado` com seus sub-campos para cada autorização que possui metadado

#### Cenário: Listagem sem metadado
- **QUANDO** uma autorização foi criada sem metadado e é listada via GET `/api/autorizacoes/listar`
- **ENTÃO** o campo `metadado` é nulo ou ausente na resposta para essa autorização

### Requisito: Mapear metadado através das camadas de domínio

O sistema DEVE mapear corretamente o objeto `metadado` do DTO de requisição através da entidade de domínio, mappers e repositório, mantendo integridade dos dados.

#### Cenário: Mapeamento DTO para domínio
- **QUANDO** `CriarAutorizacaoRequest` contendo um objeto `metadado` é processado por um use case
- **ENTÃO** o mapper (MapStruct) mapeia corretamente para o campo `metadado` da entidade `Autorizacao`

#### Cenário: Persistência de domínio
- **QUANDO** a entidade `Autorizacao` contém um objeto `metadado` preenchido
- **ENTÃO** o valor é persistido através do repositório sem perda ou corrupção de dados

#### Cenário: Recuperação de domínio
- **QUANDO** uma autorização com metadado é recuperada do banco de dados
- **ENTÃO** o objeto `metadado` é desserializado corretamente com todos os campos intactos
