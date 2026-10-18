package com.pdv.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 20)
    private String cpfCnpj;

    @Column(length = 20)
    private String rgInscricao;

    @Column(length = 100)
    private String endereco;

    @Column(length = 50)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(length = 10)
    private String cep;

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String email;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @PrePersist
    public void prePersist() {
        if (dataCadastro == null) dataCadastro = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getRgInscricao() { return rgInscricao; }
    public void setRgInscricao(String rgInscricao) { this.rgInscricao = rgInscricao; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
