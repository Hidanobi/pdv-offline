package com.pdv.repository;

import com.pdv.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findByCodigoBarras(String codigoBarras);

    List<Produto> findByAtivoTrue();

    @Query("SELECT p FROM Produto p WHERE p.estoqueAtual <= p.estoqueMinimo")
    List<Produto> findProdutosEstoqueBaixo();

    List<Produto> findByNomeContainingIgnoreCase(String nome);
}
