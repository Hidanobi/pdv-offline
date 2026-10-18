package com.pdv.controller;

import com.pdv.model.Produto;
import com.pdv.service.ProdutoService;
import com.pdv.service.SyncQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private SyncQueueService syncQueueService;

    @GetMapping
    public List<Produto> listarTodos() {
        return produtoService.listarTodos();
    }

    @GetMapping("/ativos")
    public List<Produto> listarAtivos() {
        return produtoService.listarAtivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        return produtoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigoBarras/{codigo}")
    public ResponseEntity<Produto> buscarPorCodigoBarras(@PathVariable String codigo) {
        return produtoService.buscarPorCodigoBarras(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Produto> buscarPorNome(@RequestParam String nome) {
        return produtoService.buscarPorNome(nome);
    }

    @GetMapping("/estoque-baixo")
    public List<Produto> estoqueBaixo() {
        return produtoService.estoqueBaixo();
    }

    @PostMapping
    public Produto criar(@RequestBody Produto produto) {
        Produto salvo = produtoService.salvar(produto);
        syncQueueService.registrar("PRODUTO", salvo.getId(), "CREATE", salvo);
        return salvo;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto produto) {
        return produtoService.buscarPorId(id).map(existente -> {
            produto.setId(id);
            Produto atualizado = produtoService.salvar(produto);
            syncQueueService.registrar("PRODUTO", id, "UPDATE", atualizado);
            return ResponseEntity.ok(atualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/estoque")
    public ResponseEntity<Produto> atualizarEstoque(@PathVariable Long id, @RequestParam Integer quantidade) {
        Produto produto = produtoService.atualizarEstoque(id, quantidade);
        syncQueueService.registrar("PRODUTO", id, "UPDATE", produto);
        return ResponseEntity.ok(produto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        produtoService.deletar(id);
        syncQueueService.registrar("PRODUTO", id, "DELETE", null);
        return ResponseEntity.noContent().build();
    }
}
