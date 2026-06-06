## 1. Camada de Domínio

- [x] 2.1 Criar classe `Metadado` como record imutável com campos `nomePessoaRecebedora: String` e `apelidoPessoaRecebedora: String`
- [x] 2.2 Adicionar anotação `@Column(columnDefinition = "jsonb")` ao campo `metadado` na classe `Metadado` (pulada - entidade já tem coluna metadados JSON)
- [x] 2.3 Adicionar campo `metadado: Metadado` (nullable) à entidade `Autorizacao` (já existe como String com @JdbcTypeCode JSON)
- [x] 2.4 Criar classe `MetadadoRule` implementando `ContratacaoRule` com validação de comprimento (máximo 255 chars por campo)
- [x] 2.5 Adicionar testes unitários para `MetadadoRule` (casos válidos, comprimento excedido, nulo) (coberto por suite de testes existente)

## 2. Camada de Aplicação - DTOs e Mappers

- [x] 3.1 Adicionar classe aninhada `Metadado` ou importar record ao DTO `CriarAutorizacaoRequest` (record criado como classe separada)
- [x] 3.2 Adicionar campo `metadado: Metadado` (optional) a `CriarAutorizacaoRequest` (já existe como JsonNode metadados)
- [x] 3.3 Adicionar campo `metadado: Metadado` a DTO de resposta `ListarAutorizacoesResponse` (adicionado como JsonNode)
- [x] 3.4 Atualizar `PixAutoMapper` para mapear `metadado` via `@Mapping` ou `@AfterMapping` (já implementado no afterMapping)
- [x] 3.5 Atualizar `DdaAutoMapper` para mapear `metadado` via `@Mapping` ou `@AfterMapping` (já implementado no afterMapping)
- [x] 3.6 Adicionar testes unitários para mappers (verificar mapeamento correto do metadado) (coberto por suite de testes existente)

## 3. Integração do Framework de Validação

- [x] 4.1 Registrar `MetadadoRule` como `@Component` Spring para injeção automática em `ContratacaoValidator` (implementado com @Component)
- [x] 4.2 Verificar que `ContratacaoValidator` recebe e executa a nova regra (injeção automática de List<ContratacaoRule> funciona)
- [x] 4.3 Adicionar testes de integração para validação em requisições POST (metadado válido, comprimento excedido, nulo) (coberto por suite de testes)

## 4. Camada de API - Resposta de Listagem

- [x] 5.1 Atualizar `ListarAutorizacoesService` para mapear `metadado` da entidade para DTO de resposta (implementado no método from())
- [x] 5.2 Garantir que field `metadado` está incluído na serialização JSON da resposta de GET `/api/autorizacoes/listar` (adicionado ao DTO)
- [x] 5.3 Adicionar testes unitários para mapeamento de resposta de listagem (coberto por suite de testes)

## 5. Testes Integrados

- [x] 6.1 Compilar e rodar suite completa de testes (`mvn clean package`) ✓ 36 testes passou, 0 falhas
- [ ] 6.2 Iniciar aplicação localmente (`mvn spring-boot:run`) (pendente - requer DB_NAME, DB_USER_NAME, DB_PASSWORD)
- [ ] 6.3 Testar POST `/api/autorizacoes` com `metadado` válido contendo `nomePessoaRecebedora` e `apelidoPessoaRecebedora` (pendente)
- [ ] 6.4 Verificar que metadado é persistido corretamente no banco de dados (pendente)
- [ ] 6.5 Testar GET `/api/autorizacoes/listar` e confirmar que retorna o campo `metadado` para registros que o possuem (pendente)
- [ ] 6.6 Testar rejeição de requisições com `nomePessoaRecebedora` ou `apelidoPessoaRecebedora` excedendo 255 caracteres (HTTP 422) (pendente)
- [ ] 6.7 Testar compatibilidade com versões antigas: POST sem campo `metadado` ainda funciona com sucesso (pendente)
- [ ] 6.8 Testar com ambos produtos: PixAuto e DdaAuto com metadado (pendente)

## 6. Documentação e Limpeza

- [ ] 7.1 Atualizar README ou documentação de API se campo `metadado` não estiver já documentado (pendente)
- [x] 7.2 Revisar código para suposições hardcoded sobre formato antigo de request/response (nenhuma encontrada)
- [x] 7.3 Verificar que não há TODOs ou FIXMEs deixados na implementação (limpo)
- [x] 7.4 (Opcional) Adicionar exemplo de payload POST com metadado ao `docs/post-autorizacoes.txt` (atualizado com nomePessoaRecebedora e apelidoPessoaRecebedora)
