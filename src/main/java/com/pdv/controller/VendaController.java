package com.pdv.controller;

import com.pdv.model.Venda;
import com.pdv.service.FiscalService;
import com.pdv.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/vendas")
@CrossOrigin(origins = "*")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private FiscalService fiscalService;

    @GetMapping
    public List<Venda> listarTodos() {
        return vendaService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venda> buscarPorId(@PathVariable Long id) {
        return vendaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/periodo")
    public List<Venda> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return vendaService.buscarPorPeriodo(inicio.atStartOfDay(), fim.atTime(LocalTime.MAX));
    }

    /**
     * Finaliza uma venda. Calcula totais, baixa estoque, emite documento fiscal
     * e registra na fila de sincronizacao offline.
     */
    @PostMapping("/finalizar")
    public ResponseEntity<Venda> finalizarVenda(@RequestBody Venda venda) {
        try {
            Venda finalizada = vendaService.finalizarVenda(venda);
            return ResponseEntity.ok(finalizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/nao-sincronizadas")
    public List<Venda> naoSincronizadas() {
        return vendaService.vendasNaoSincronizadas();
    }

    @GetMapping("/fiscal/{vendaId}")
    public ResponseEntity<?> documentoFiscalPorVenda(@PathVariable Long vendaId) {
        return fiscalService.buscarPorVenda(vendaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fiscal")
    public List<com.pdv.model.DocumentoFiscal> todosDocumentosFiscais() {
        return fiscalService.listarTodos();
    }

    @GetMapping("/fiscal/danfe/{id}")
    public ResponseEntity<String> visualizarDanfe(@PathVariable Long id) {
        return fiscalService.buscarPorId(id)
                .map(doc -> ResponseEntity.ok()
                        .header("Content-Type", "text/plain; charset=UTF-8")
                        .body(doc.getDanfeConteudo()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fiscal/xml/{id}")
    public ResponseEntity<String> visualizarXml(@PathVariable Long id) {
        return fiscalService.buscarPorId(id)
                .map(doc -> ResponseEntity.ok()
                        .header("Content-Type", "application/xml; charset=UTF-8")
                        .body(doc.getXmlConteudo()))
                .orElse(ResponseEntity.notFound().build());
    }
}
