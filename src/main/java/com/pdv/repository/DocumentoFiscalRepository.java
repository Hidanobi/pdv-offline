package com.pdv.repository;

import com.pdv.model.DocumentoFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoFiscalRepository extends JpaRepository<DocumentoFiscal, Long> {

    Optional<DocumentoFiscal> findByVendaId(Long vendaId);

    List<DocumentoFiscal> findByModelo(String modelo);

    List<DocumentoFiscal> findByStatus(String status);
}
