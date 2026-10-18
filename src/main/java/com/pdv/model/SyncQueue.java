package com.pdv.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Fila de sincronizacao offline.
 * Armazena operacoes pendentes para envio ao servidor central quando online.
 */
@Entity
@Table(name = "sync_queue")
public class SyncQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String entidade; // VENDA, PRODUTO, CLIENTE, DOCUMENTO_FISCAL

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(length = 20, nullable = false)
    private String operacao; // CREATE, UPDATE, DELETE

    @Lob
    @Column(name = "dados_json", columnDefinition = "CLOB")
    private String dadosJson;

    @Column(length = 20)
    private String status; // PENDENTE, ENVIADO, ERRO

    @Column(name = "tentativas")
    private Integer tentativas = 0;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @Column(name = "mensagem_erro", length = 1000)
    private String mensagemErro;

    @PrePersist
    public void prePersist() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
        if (status == null) status = "PENDENTE";
        if (tentativas == null) tentativas = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEntidade() { return entidade; }
    public void setEntidade(String entidade) { this.entidade = entidade; }

    public Long getEntidadeId() { return entidadeId; }
    public void setEntidadeId(Long entidadeId) { this.entidadeId = entidadeId; }

    public String getOperacao() { return operacao; }
    public void setOperacao(String operacao) { this.operacao = operacao; }

    public String getDadosJson() { return dadosJson; }
    public void setDadosJson(String dadosJson) { this.dadosJson = dadosJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTentativas() { return tentativas; }
    public void setTentativas(Integer tentativas) { this.tentativas = tentativas; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public String getMensagemErro() { return mensagemErro; }
    public void setMensagemErro(String mensagemErro) { this.mensagemErro = mensagemErro; }
}
