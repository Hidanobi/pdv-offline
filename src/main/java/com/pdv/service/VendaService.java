package com.pdv.service;

import com.pdv.model.ItemVenda;
import com.pdv.model.Produto;
import com.pdv.model.Venda;
import com.pdv.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private SyncQueueService syncQueueService;

    @Autowired
    private FiscalService fiscalService;

    /**
     * Finaliza uma venda: calcula totais, baixa estoque, gera numero,
     * registra na fila de sincronizacao e emite documento fiscal.
     */
    @Transactional
    public Venda finalizarVenda(Venda venda) {
        // Calcula totais
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemVenda item : venda.getItens()) {
            item.setVenda(venda);
            if (item.getDesconto() == null) item.setDesconto(BigDecimal.ZERO);
            BigDecimal totalItem = item.getPrecoUnitario()
                    .multiply(item.getQuantidade())
                    .subtract(item.getDesconto());
            item.setValorTotal(totalItem);
            subtotal = subtotal.add(totalItem);
        }
        venda.setSubtotal(subtotal);
        if (venda.getDesconto() == null) venda.setDesconto(BigDecimal.ZERO);
        venda.setTotal(subtotal.subtract(venda.getDesconto()));

        if (venda.getValorPago() == null) venda.setValorPago(venda.getTotal());
        if (venda.getTroco() == null) {
            venda.setTroco(venda.getValorPago().subtract(venda.getTotal()));
        }

        // Gera numero da venda
        venda.setNumeroVenda(gerarNumeroVenda());

        if (venda.getStatus() == null) venda.setStatus("FINALIZADA");

        Venda vendaSalva = vendaRepository.save(venda);

        // Baixa de estoque
        for (ItemVenda item : venda.getItens()) {
            try {
                produtoService.decrementarEstoque(item.getProduto().getId(),
                        item.getQuantidade().intValue());
            } catch (Exception e) {
                // Loga mas nao interrompe a venda
                System.err.println("Erro ao baixar estoque do produto " +
                        item.getProduto().getId() + ": " + e.getMessage());
            }
        }

        // Registra na fila de sincronizacao (offline)
        try {
            syncQueueService.registrarVenda(vendaSalva);
        } catch (Exception e) {
            System.err.println("Erro ao registrar venda na fila de sync: " + e.getMessage());
        }

        // Emite documento fiscal (NFC-e ou Cupom Fiscal)
        try {
            fiscalService.emitirDocumentoFiscal(vendaSalva);
        } catch (Exception e) {
            System.err.println("Erro ao emitir documento fiscal: " + e.getMessage());
        }

        return vendaSalva;
    }

    private String gerarNumeroVenda() {
        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequencia = vendaRepository.count() + 1;
        return String.format("V%s%06d", data, sequencia);
    }

    public List<Venda> listarTodos() {
        return vendaRepository.findAll();
    }

    public Optional<Venda> buscarPorId(Long id) {
        return vendaRepository.findById(id);
    }

    public List<Venda> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return vendaRepository.findByPeriodo(inicio, fim);
    }

    public BigDecimal totalVendasPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return vendaRepository.sumTotalByPeriodo(inicio, fim);
    }

    public Long quantidadeVendasPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return vendaRepository.countByPeriodo(inicio, fim);
    }

    public List<Venda> vendasNaoSincronizadas() {
        return vendaRepository.findBySincronizadaFalse();
    }

    public void marcarComoSincronizada(Long id) {
        Venda venda = vendaRepository.findById(id).orElse(null);
        if (venda != null) {
            venda.setSincronizada(true);
            venda.setDataSincronizacao(LocalDateTime.now());
            vendaRepository.save(venda);
        }
    }
}
