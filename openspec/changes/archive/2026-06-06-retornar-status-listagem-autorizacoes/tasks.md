## 1. DTO de resposta

- [x] 1.1 Adicionar o campo `private String status;` em `AutorizacaoResumidaResponseDto` (entrypoint/contratosrest), posicionado de forma coerente com os demais campos.
- [x] 1.2 No factory `from(Autorizacao)`, preencher `.status(...)` traduzindo `autorizacao.getStatus()` para o nome do enum via `StatusAutorizacao.obterStatusEnumPorIdStatus(autorizacao.getStatus()).name()`, retornando `null` quando o status da entidade for nulo.
- [x] 1.3 Importar `br.com.srportto.contratocommand.domain.enums.StatusAutorizacao` no DTO.

## 2. Testes

- [x] 2.1 Em `ListarAutorizacoesServiceTest#testConversaoParaDtoResumido`, asseverar que `dto.getStatus()` corresponde ao nome do enum esperado para o status de `autorizacao1` (código 1 → `RECEBIDA`).
- [x] 2.2 Adicionar/ajustar teste cobrindo um segundo status distinto (ex.: `autorizacao2`, código 4 → `ATIVA`) para validar que cada item reflete seu próprio status.

## 3. Verificação

- [x] 3.1 Rodar `mvn test -Dtest=ListarAutorizacoesServiceTest` e confirmar que passa.
- [x] 3.2 Rodar `mvn clean compile` sem erros.
- [ ] 3.3 (Opcional) Subir a aplicação e validar manualmente que `GET /api/autorizacoes/listar` retorna o campo `status` em cada item, conforme `docs/post-autorizacoes.txt`.
