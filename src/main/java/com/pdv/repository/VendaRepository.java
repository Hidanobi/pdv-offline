package com.pdv.repository;

import com.pdv.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findBySincronizadaFalse();

    @Query("SELECT v FROM Venda v WHERE v.dataVenda >= :inicio AND v.dataVenda <= :fim ORDER BY v.dataVenda DESC")
    List<Venda> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venda v WHERE v.dataVenda >= :inicio AND v.dataVenda <= :fim AND v.status = 'FINALIZADA'")
    java.math.BigDecimal sumTotalByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(v) FROM Venda v WHERE v.dataVenda >= :inicio AND v.dataVenda <= :fim AND v.status = 'FINALIZADA'")
    Long countByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
