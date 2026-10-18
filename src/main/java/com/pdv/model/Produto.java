package com.pdv.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 14)
    private String codigoBarras;

    @Column(nullable = false)
    private String nome;

    @Column(length = 50)
    private String categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVenda;

    @Column(precision = 12, scale = 2)
    private BigDecimal precoCusto;

    @Column(nullable = false)
    private Integer estoqueAtual;

    private Integer estoqueMinimo;

    @Column(length = 10)
    private String unidade; // UN, KG, LT, CX

    @Column(length = 12)
    private String ncm; // Nomenclatura Comum do Mercosul (ex: 2101.10.10)

    @Column(length = 10)
    private String cest;

    @Column(precision = 5, scale = 2)
    private BigDecimal aliquotaIcms;

    private Boolean ativo = true;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @PrePersist
    public void prePersist() {
        if (dataCadastro == null) dataCadastro = LocalDateTime.now();
        if (estoqueMinimo == null) estoqueMinimo = 0;
        if (estoqueAtual == null) estoqueAtual = 0;
        if (unidade == null) unidade = "UN";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(BigDecimal precoVenda) { this.precoVenda = precoVenda; }

    public BigDecimal getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(BigDecimal precoCusto) { this.precoCusto = precoCusto; }

    public Integer getEstoqueAtual() { return estoqueAtual; }
    public void setEstoqueAtual(Integer estoqueAtual) { this.estoqueAtual = estoqueAtual; }

    public Integer getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(Integer estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public String getNcm() { return ncm; }
    public void setNcm(String ncm) { this.ncm = ncm; }

    public String getCest() { return cest; }
    public void setCest(String cest) { this.cest = cest; }

    public BigDecimal getAliquotaIcms() { return aliquotaIcms; }
    public void setAliquotaIcms(BigDecimal aliquotaIcms) { this.aliquotaIcms = aliquotaIcms; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
