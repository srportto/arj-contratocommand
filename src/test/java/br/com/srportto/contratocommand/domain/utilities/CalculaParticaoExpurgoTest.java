package br.com.srportto.contratocommand.domain.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@DisplayName("Testes da classe CalculaParticaoExpurgo")
class CalculaParticaoExpurgoTest {

  private static final LocalDate EPOCH_DAY = LocalDate.ofEpochDay(0); // 1970-01-01

  @Test
  @DisplayName("Deve retornar 900 para a data de época (1970-01-01)")
  void testEpochDay() {
    int resultado = CalculaParticaoExpurgo.obterParticaoExpurgo(EPOCH_DAY);
    assertEquals(900, resultado);
  }

  @Test
  @DisplayName("Deve retornar 901 para 7 dias após a época")
  void testOneWeekAfterEpoch() {
    LocalDate umaSemanaApos = EPOCH_DAY.plusWeeks(1);
    int resultado = CalculaParticaoExpurgo.obterParticaoExpurgo(umaSemanaApos);
    assertEquals(901, resultado);
  }

  @Test
  @DisplayName("Deve retornar 999 para 99 semanas após a época")
  void test99WeeksAfterEpoch() {
    LocalDate noventaNoveSemanasApos = EPOCH_DAY.plusWeeks(99);
    int resultado = CalculaParticaoExpurgo.obterParticaoExpurgo(noventaNoveSemanasApos);
    assertEquals(999, resultado);
  }

  @Test
  @DisplayName("Deve retornar 900 para 100 semanas após a época (ciclo volta para 0)")
  void test100WeeksAfterEpoch() {
    LocalDate cem = EPOCH_DAY.plusWeeks(100);
    int resultado = CalculaParticaoExpurgo.obterParticaoExpurgo(cem);
    assertEquals(900, resultado);
  }

  @Test
  @DisplayName("Deve retornar 950 para 50 semanas após a época")
  void test50WeeksAfterEpoch() {
    LocalDate cinquentaSemanasApos = EPOCH_DAY.plusWeeks(50);
    int resultado = CalculaParticaoExpurgo.obterParticaoExpurgo(cinquentaSemanasApos);
    assertEquals(950, resultado);
  }

  @ParameterizedTest(name = "Semanas = {0}, Partição esperada = {1}")
  @ValueSource(ints = {0, 1, 5, 10, 25, 50, 75, 99, 100, 150, 199, 200})
  @DisplayName("Deve calcular corretamente a partição para várias semanas")
  void testMultiplasSemanasComPartição(int semanas) {
    LocalDate data = EPOCH_DAY.plusWeeks(semanas);
    int resultado = CalculaParticaoExpurgo.obterParticaoExpurgo(data);
    
    int partição = (semanas % 100) + 900;
    assertEquals(partição, resultado, 
        "Para " + semanas + " semanas, esperava " + partição + " mas obteve " + resultado);
  }

  @Test
  @DisplayName("Deve gerar todas as partições de 900 a 999")
  void testTodosOsValoresNo900A999() {
    Set<Integer> particoesGeradas = new HashSet<>();

    // Testa 1000 semanas diferentes para garantir cobertura completa do range 900-999
    for (int semanas = 0; semanas < 1000; semanas++) {
      LocalDate data = EPOCH_DAY.plusWeeks(semanas);
      int particao = CalculaParticaoExpurgo.obterParticaoExpurgo(data);
      particoesGeradas.add(particao);
    }

    // Valida que todos os valores de 900 a 999 foram gerados
    assertEquals(100, particoesGeradas.size(), "Deveria gerar exatamente 100 valores diferentes");
    
    for (int i = 900; i <= 999; i++) {
      assertTrue(particoesGeradas.contains(i), 
          "A partição " + i + " não foi gerada no intervalo de 1000 semanas testadas");
    }
  }

  @Test
  @DisplayName("Deve estar sempre entre 900 e 999")
  void testResultadoSempreNoRange() {
    // Testa datas em vários períodos
    LocalDate[] datas = {
        LocalDate.of(1970, 1, 1),    // Época
        LocalDate.of(2000, 1, 1),    // Ano 2000
        LocalDate.of(2026, 4, 18),   // Hoje
        LocalDate.of(2050, 12, 31),  // Futuro próximo
        LocalDate.of(2100, 6, 15)    // Futuro distante
    };

    for (LocalDate data : datas) {
      int resultado = CalculaParticaoExpurgo.obterParticaoExpurgo(data);
      assertTrue(resultado >= 900 && resultado <= 999, 
          "Resultado " + resultado + " para data " + data + " está fora do range [900, 999]");
    }
  }

  @Test
  @DisplayName("Deve respeitar a fórmula (semanas % 100) + 900")
  void testFórmulaExata() {
    // Testa 300 semanas para validar a fórmula
    for (int semanas = 0; semanas < 300; semanas++) {
      LocalDate data = EPOCH_DAY.plusWeeks(semanas);
      int resultado = CalculaParticaoExpurgo.obterParticaoExpurgo(data);
      
      int esperado = (semanas % 100) + 900;
      assertEquals(esperado, resultado, 
          "Fórmula falhou para " + semanas + " semanas");
    }
  }

  @Test
  @DisplayName("Deve manter consistência para mesma data")
  void testConsistênciaParaMesmaData() {
    LocalDate data = LocalDate.of(2026, 4, 18);
    
    int resultado1 = CalculaParticaoExpurgo.obterParticaoExpurgo(data);
    int resultado2 = CalculaParticaoExpurgo.obterParticaoExpurgo(data);
    int resultado3 = CalculaParticaoExpurgo.obterParticaoExpurgo(data);

    assertEquals(resultado1, resultado2, "Resultados deveriam ser iguais para mesma data");
    assertEquals(resultado2, resultado3, "Resultados deveriam ser iguais para mesma data");
  }
}
