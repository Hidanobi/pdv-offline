package com.pdv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Sistema PDV Offline
 * - Funciona sem internet (banco H2 em arquivo local)
 * - Sincroniza estoque/vendas com servidor central quando online
 * - Emite NFC-e e Cupom Fiscal (modo simulacao com estrutura real)
 * - Integrado com leitor de codigo de barras
 */
@SpringBootApplication
@EnableScheduling
public class PdvOfflineApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdvOfflineApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  SISTEMA PDV OFFLINE INICIADO");
        System.out.println("  Acesse: http://localhost:8080");
        System.out.println("  Console H2: http://localhost:8080/h2-console");
        System.out.println("========================================\n");
    }
}
