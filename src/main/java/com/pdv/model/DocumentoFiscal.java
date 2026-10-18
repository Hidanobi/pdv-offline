package com.pdv.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos_fiscais")
public class DocumentoFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id")
    @JsonIgnore
    private Venda venda;

    @Column(length = 10)
    private String modelo; // 65 = NFC-e, 59 = Cupom Fiscal (SAT), 55 = NF-e

    @Column(length = 50)
    private String numero;

    @Column(length = 10)
    private String serie;

    @Column(name = "chave_acesso", length = 44)
    private String chaveAcesso;

    @Column(name = "protocolo", length = 50)
    private String protocolo;

    @Column(length = 20)
    private String status; // AUTORIZADO, REJEITADO, CANCELADO, PENDENTE, SIMULADO

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Column(name = "data_autorizacao")
    private LocalDateTime dataAutorizacao;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_icms", precision = 12, scale = 2)
    private BigDecimal valorIcms;

    @Lob
    @Column(name = "xml_conteudo", columnDefinition = "CLOB")
    private String xmlConteudo;

    @Lob
    @Column(name = "danfe_conteudo", columnDefinition = "CLOB")
    private String danfeConteudo;

    @Column(name = "ambiente", length = 20)
    private String ambiente; // homologacao, producao

    @Column(name = "sincronizado")
    private Boolean sincronizado = false;

    @PrePersist
    public void prePersist() {
        if (dataEmissao == null) dataEmissao = LocalDateTime.now();
        if (sincronizado == null) sincronizado = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Venda getVenda() { return venda; }
    public void setVenda(Venda venda) { this.venda = venda; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public String getChaveAcesso() { return chaveAcesso; }
    public void setChaveAcesso(String chaveAcesso) { this.chaveAcesso = chaveAcesso; }

    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDateTime dataEmissao) { this.dataEmissao = dataEmissao; }

    public LocalDateTime getDataAutorizacao() { return dataAutorizacao; }
    public void setDataAutorizacao(LocalDateTime dataAutorizacao) { this.dataAutorizacao = dataAutorizacao; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public BigDecimal getValorIcms() { return valorIcms; }
    public void setValorIcms(BigDecimal valorIcms) { this.valorIcms = valorIcms; }

    public String getXmlConteudo() { return xmlConteudo; }
    public void setXmlConteudo(String xmlConteudo) { this.xmlConteudo = xmlConteudo; }

    public String getDanfeConteudo() { return danfeConteudo; }
    public void setDanfeConteudo(String danfeConteudo) { this.danfeConteudo = danfeConteudo; }

    public String getAmbiente() { return ambiente; }
    public void setAmbiente(String ambiente) { this.ambiente = ambiente; }

    public Boolean getSincronizado() { return sincronizado; }
    public void setSincronizado(Boolean sincronizado) { this.sincronizado = sincronizado; }
}
