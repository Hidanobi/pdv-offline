package com.pdv.config;

import com.pdv.model.Cliente;
import com.pdv.model.Produto;
import com.pdv.repository.ClienteRepository;
import com.pdv.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Inicializa dados de exemplo na primeira execucao.
 * Apenas insere se o banco estiver vazio.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public void run(String... args) {
        if (produtoRepository.count() == 0) {
            inserirProdutosExemplo();
        }
        if (clienteRepository.count() == 0) {
            inserirClientesExemplo();
        }
    }

    private void inserirProdutosExemplo() {
        Produto[] produtos = {
                criarProduto("7891000000001", "Cafe 500g", "Alimentos", new BigDecimal("18.90"), new BigDecimal("12.00"), 150, 20, "UN", "2101.10.10", null, new BigDecimal("0")),
                criarProduto("7891000000002", "Leite Integral 1L", "Bebidas", new BigDecimal("5.49"), new BigDecimal("3.50"), 80, 30, "UN", "0401.20.10", null, new BigDecimal("0")),
                criarProduto("7891000000003", "Pao de Forma", "Padaria", new BigDecimal("7.50"), new BigDecimal("4.00"), 45, 15, "UN", "1905.90.10", null, new BigDecimal("0")),
                criarProduto("7891000000004", "Arroz 5kg", "Alimentos", new BigDecimal("27.90"), new BigDecimal("20.00"), 60, 10, "UN", "1006.30.12", null, new BigDecimal("0")),
                criarProduto("7891000000005", "Feijao 1kg", "Alimentos", new BigDecimal("8.99"), new BigDecimal("5.50"), 100, 20, "UN", "0713.31.00", null, new BigDecimal("0")),
                criarProduto("7891000000006", "Acucar 1kg", "Alimentos", new BigDecimal("4.99"), new BigDecimal("3.00"), 120, 25, "UN", "1701.91.00", null, new BigDecimal("0")),
                criarProduto("7891000000007", "Oleo de Soja 900ml", "Alimentos", new BigDecimal("6.79"), new BigDecimal("4.50"), 5, 20, "UN", "1507.10.00", null, new BigDecimal("0")),
                criarProduto("7891000000008", "Detergente 500ml", "Limpeza", new BigDecimal("2.49"), new BigDecimal("1.20"), 200, 50, "UN", "3402.20.10", null, new BigDecimal("0")),
                criarProduto("7891000000009", "Sabonete", "Higiene", new BigDecimal("1.99"), new BigDecimal("0.80"), 300, 100, "UN", "3401.11.00", null, new BigDecimal("0")),
                criarProduto("7891000000010", "Papel Higienico 12un", "Higiene", new BigDecimal("12.90"), new BigDecimal("8.00"), 3, 15, "UN", "4818.10.00", null, new BigDecimal("0")),
                criarProduto("7891000000011", "Macarrao 500g", "Alimentos", new BigDecimal("3.49"), new BigDecimal("1.80"), 180, 30, "UN", "1902.19.00", null, new BigDecimal("0")),
                criarProduto("7891000000012", "Refrigerante 2L", "Bebidas", new BigDecimal("9.99"), new BigDecimal("6.00"), 90, 25, "UN", "2202.10.00", null, new BigDecimal("0")),
        };
        for (Produto p : produtos) {
            produtoRepository.save(p);
        }
        System.out.println("[INIT] " + produtos.length + " produtos de exemplo inseridos.");
    }

    private Produto criarProduto(String cb, String nome, String cat, BigDecimal pv, BigDecimal pc,
                                 int estoque, int minimo, String un, String ncm, String cest, BigDecimal icms) {
        Produto p = new Produto();
        p.setCodigoBarras(cb);
        p.setNome(nome);
        p.setCategoria(cat);
        p.setPrecoVenda(pv);
        p.setPrecoCusto(pc);
        p.setEstoqueAtual(estoque);
        p.setEstoqueMinimo(minimo);
        p.setUnidade(un);
        p.setNcm(ncm);
        p.setCest(cest);
        p.setAliquotaIcms(icms);
        p.setAtivo(true);
        return p;
    }

    private void inserirClientesExemplo() {
        Cliente c1 = new Cliente();
        c1.setNome("Consumidor Final");
        c1.setCpfCnpj("");
        clienteRepository.save(c1);

        Cliente c2 = new Cliente();
        c2.setNome("Joao da Silva");
        c2.setCpfCnpj("12345678901");
        c2.setCidade("Sao Paulo");
        c2.setUf("SP");
        c2.setTelefone("11999998888");
        clienteRepository.save(c2);

        Cliente c3 = new Cliente();
        c3.setNome("Maria Souza");
        c3.setCpfCnpj("98765432100");
        c3.setCidade("Sao Paulo");
        c3.setUf("SP");
        c3.setEmail("maria@email.com");
        clienteRepository.save(c3);

        System.out.println("[INIT] Clientes de exemplo inseridos.");
    }
}
