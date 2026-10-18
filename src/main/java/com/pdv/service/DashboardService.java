package com.pdv.service;

import com.pdv.model.Produto;
import com.pdv.model.Venda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private SyncQueueService syncQueueService;

    /**
     * Retorna dados consolidados para o dashboard.
     */
    public Map<String, Object> getDadosDashboard() {
        Map<String, Object> dados = new LinkedHashMap<>();

        // Vendas de hoje
        LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();
        LocalDateTime fimHoje = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal totalHoje = vendaService.totalVendasPeriodo(inicioHoje, fimHoje);
        Long qtdVendasHoje = vendaService.quantidadeVendasPeriodo(inicioHoje, fimHoje);

        // Vendas do mes
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        BigDecimal totalMes = vendaService.totalVendasPeriodo(inicioMes, fimHoje);

        // Ticket medio
        BigDecimal ticketMedio = BigDecimal.ZERO;
        if (qtdVendasHoje > 0) {
            ticketMedio = totalHoje.divide(BigDecimal.valueOf(qtdVendasHoje), 2, BigDecimal.ROUND_HALF_UP);
        }

        dados.put("totalHoje", totalHoje);
        dados.put("qtdVendasHoje", qtdVendasHoje);
        dados.put("totalMes", totalMes);
        dados.put("ticketMedio", ticketMedio);

        // Produtos com estoque baixo
        List<Produto> estoqueBaixo = produtoService.estoqueBaixo();
        dados.put("qtdEstoqueBaixo", estoqueBaixo.size());
        dados.put("produtosEstoqueBaixo", estoqueBaixo);

        // Status de sincronizacao
        dados.put("pendentesSync", syncQueueService.contarPendentes());
        dados.put("ultimoSync", syncQueueService.ultimaSincronizacao());

        // Vendas dos ultimos 7 dias para o grafico
        List<Map<String, Object>> vendas7Dias = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate data = LocalDate.now().minusDays(i);
            BigDecimal total = vendaService.totalVendasPeriodo(
                    data.atStartOfDay(), data.atTime(LocalTime.MAX));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("data", data.toString());
            item.put("total", total);
            vendas7Dias.add(item);
        }
        dados.put("vendas7Dias", vendas7Dias);

        // Formas de pagamento (hoje)
        List<Venda> vendasHoje = vendaService.buscarPorPeriodo(inicioHoje, fimHoje);
        Map<String, BigDecimal> formasPagamento = new LinkedHashMap<>();
        for (Venda v : vendasHoje) {
            String fp = v.getFormaPagamento() != null ? v.getFormaPagamento() : "OUTROS";
            formasPagamento.merge(fp, v.getTotal(), BigDecimal::add);
        }
        dados.put("formasPagamento", formasPagamento);

        return dados;
    }
}
