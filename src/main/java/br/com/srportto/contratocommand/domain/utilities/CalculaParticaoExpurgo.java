package br.com.srportto.contratocommand.domain.utilities;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CalculaParticaoExpurgo {

  public static int obterParticaoExpurgo(LocalDate dataFinalizacao) {
        // O ChronoUnit.WEEKS calcula o número exato de semanas totais 
        // entre o Epoch (01/01/1970) e a data finalização
        long semanasTotais = ChronoUnit.WEEKS.between(LocalDate.ofEpochDay(0), dataFinalizacao);
        
        // Encontra a "gaveta" de 0 a 99
        int gaveta = (int) (semanasTotais % 100);
        
        // Soma 900 para cair na partição correta (900 a 999)
        return 900 + gaveta;
    }

}
