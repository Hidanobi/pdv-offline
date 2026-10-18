package com.pdv.service;

import com.pdv.model.Produto;
import com.pdv.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public List<Produto> listarAtivos() {
        return produtoRepository.findByAtivoTrue();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public Optional<Produto> buscarPorCodigoBarras(String codigoBarras) {
        return produtoRepository.findByCodigoBarras(codigoBarras);
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Produto> estoqueBaixo() {
        return produtoRepository.findProdutosEstoqueBaixo();
    }

    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    public Produto atualizarEstoque(Long id, Integer quantidade) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto nao encontrado: " + id));
        produto.setEstoqueAtual(quantidade);
        return produtoRepository.save(produto);
    }

    public void decrementarEstoque(Long id, Integer quantidade) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto nao encontrado: " + id));
        produto.setEstoqueAtual(produto.getEstoqueAtual() - quantidade);
        produtoRepository.save(produto);
    }

    public void deletar(Long id) {
        produtoRepository.deleteById(id);
    }
}
