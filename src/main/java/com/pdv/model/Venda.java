package com.pdv.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendas")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_venda", unique = true)
    private String numeroVenda;

    @Column(name = "data_venda", nullable = false)
    private LocalDateTime dataVenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "nome_cliente")
    private String nomeCliente;

    @Column(name = "cpf_cliente", length = 20)
    private String cpfCliente;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemVenda> itens = new ArrayList<>();

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "desconto", precision = 12, scale = 2)
    private BigDecimal desconto;

    @Column(name = "total", precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "valor_pago", precision = 12, scale = 2)
    private BigDecimal valorPago;

    @Column(name = "troco", precision = 12, scale = 2)
    private BigDecimal troco;

    @Column(length = 20)
    private String formaPagamento; // DINHEIRO, CARTAO, PIX, CREDIARIO

    @Column(length = 20)
    private String status; // ABERTA, FINALIZADA, CANCELADA

    @Column(name = "operador", length = 50)
    private String operador;

    @Column(name = "sincronizada")
    private Boolean sincronizada = false;

    @Column(name = "data_sincronizacao")
    private LocalDateTime dataSincronizacao;

    @PrePersist
    public void prePersist() {
        if (dataVenda == null) dataVenda = LocalDateTime.now();
        if (status == null) status = "FINALIZADA";
        if (desconto == null) desconto = BigDecimal.ZERO;
        if (sincronizada == null) sincronizada = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroVenda() { return numeroVenda; }
    public void setNumeroVenda(String numeroVenda) { this.numeroVenda = numeroVenda; }

    public LocalDateTime getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDateTime dataVenda) { this.dataVenda = dataVenda; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getCpfCliente() { return cpfCliente; }
    public void setCpfCliente(String cpfCliente) { this.cpfCliente = cpfCliente; }

    public List<ItemVenda> getItens() { return itens; }
    public void setItens(List<ItemVenda> itens) { this.itens = itens; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDesconto() { return desconto; }
    public void setDesconto(BigDecimal desconto) { this.desconto = desconto; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public BigDecimal getTroco() { return troco; }
    public void setTroco(BigDecimal troco) { this.troco = troco; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOperador() { return operador; }
    public void setOperador(String operador) { this.operador = operador; }

    public Boolean getSincronizada() { return sincronizada; }
    public void setSincronizada(Boolean sincronizada) { this.sincronizada = sincronizada; }

    public LocalDateTime getDataSincronizacao() { return dataSincronizacao; }
    public void setDataSincronizacao(LocalDateTime dataSincronizacao) { this.dataSincronizacao = dataSincronizacao; }
}
