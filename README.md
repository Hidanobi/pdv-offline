# PDV Offline

Sistema de Ponto de Venda (PDV) que funciona **100% offline**, com banco de dados local, emissão de documento fiscal em modo simulação e fila de sincronização para envio posterior a um servidor central.

O sistema não depende de internet para vender: os dados são gravados em um banco H2 local e as operações ficam registradas em uma fila para sincronização quando houver conexão.

---

## Funcionalidades

- **PDV / Caixa** — frente de caixa com busca de produto por código de barras (compatível com leitor), múltiplos itens, desconto, cálculo de total e troco.
- **Produtos e estoque** — cadastro, edição, exclusão, busca por nome/código de barras, controle de estoque, estoque mínimo e alerta de estoque baixo.
- **Clientes** — cadastro, consulta por nome ou CPF/CNPJ.
- **Histórico de vendas** — lista de vendas e consulta por período.
- **Fiscal** — emissão de documento fiscal em **modo simulação**: gera chave de acesso e conteúdo XML/DANFE estruturado, sem envio à SEFAZ.
- **Sincronização offline** — cada alteração (produto, cliente, venda) é registrada em uma fila; há painel de status, sincronização manual e limpeza de registros já enviados.
- **Dashboard** — visão geral de vendas do dia, faturamento, produtos com estoque baixo e pendências de sincronização.

---

## Tecnologias

- **Java 17**
- **Spring Boot 3.2.5** (Web, Data JPA, Validation)
- **Hibernate / JPA**
- **H2 Database** (arquivo local — funciona offline)
- **Jackson** (XML/JSON)
- **Lombok**
- **Maven**
- Front-end em **HTML + CSS + JavaScript** (páginas estáticas servidas pelo próprio Spring Boot)

---

## Pré-requisitos

- JDK 17
- Maven 3.9+

---

## Como executar

### Opção 1 — script pronto (Windows)

Ajuste, se necessário, os caminhos de `JAVA_HOME` e `MAVEN_HOME` no arquivo `run.bat` e execute:

```bat
run.bat
```

### Opção 2 — linha de comando

```bash
mvn spring-boot:run
```

### Opção 3 — empacotar e rodar o JAR

```bash
mvn clean package -DskipTests
java -jar target/pdv-offline-1.0.0.jar
```

Depois acesse:

```
http://localhost:8080
```

O banco de dados é criado automaticamente em `./data/pdvdb` na primeira execução, junto com dados de exemplo.

---

## Telas

| Página | Caminho |
|---|---|
| Dashboard | `http://localhost:8080/` |
| PDV / Caixa | `/pdv.html` |
| Produtos e estoque | `/produtos.html` |
| Clientes | `/clientes.html` |
| Histórico de vendas | `/historico.html` |
| Documentos fiscais | `/fiscais.html` |
| Sincronização | `/sincronizacao.html` |

---

## API (principais endpoints)

### Dashboard
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/dashboard` | Indicadores gerais |

### Produtos
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/produtos` | Lista todos |
| GET | `/api/produtos/ativos` | Lista apenas ativos |
| GET | `/api/produtos/{id}` | Busca por ID |
| GET | `/api/produtos/codigoBarras/{codigo}` | Busca por código de barras |
| GET | `/api/produtos/buscar?nome=` | Busca por nome |
| GET | `/api/produtos/estoque-baixo` | Produtos com estoque baixo |
| POST | `/api/produtos` | Cria produto |
| PUT | `/api/produtos/{id}` | Atualiza produto |
| PUT | `/api/produtos/{id}/estoque?quantidade=` | Atualiza estoque |
| DELETE | `/api/produtos/{id}` | Remove produto |

### Clientes
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/clientes` | Lista todos |
| GET | `/api/clientes/{id}` | Busca por ID |
| GET | `/api/clientes/cpf/{cpf}` | Busca por CPF/CNPJ |
| GET | `/api/clientes/buscar?nome=` | Busca por nome |
| POST | `/api/clientes` | Cria cliente |
| PUT | `/api/clientes/{id}` | Atualiza cliente |
| DELETE | `/api/clientes/{id}` | Remove cliente |

### Vendas
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/vendas` | Lista todas |
| GET | `/api/vendas/{id}` | Busca por ID |
| GET | `/api/vendas/periodo?inicio=&fim=` | Busca por período (AAAA-MM-DD) |
| GET | `/api/vendas/nao-sincronizadas` | Vendas ainda não sincronizadas |
| POST | `/api/vendas/finalizar` | Finaliza a venda (baixa estoque, emite documento fiscal e enfileira sincronização) |

### Fiscal
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/vendas/fiscal` | Lista documentos fiscais |
| GET | `/api/vendas/fiscal/{vendaId}` | Documento fiscal de uma venda |
| GET | `/api/vendas/fiscal/danfe/{id}` | Conteúdo do DANFE |
| GET | `/api/vendas/fiscal/xml/{id}` | XML do documento |

### Sincronização
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/sync/status` | Pendências e última sincronização |
| GET | `/api/sync/pendentes` | Registros pendentes |
| POST | `/api/sync/sincronizar` | Dispara sincronização manual |
| DELETE | `/api/sync/limpar` | Remove registros já enviados |

---

## Estrutura do projeto

```
src/main/java/com/pdv/
├── PdvOfflineApplication.java
├── config/         # Carga de dados iniciais
├── controller/     # Endpoints REST (Dashboard, Produto, Cliente, Venda, Sync)
├── model/          # Entidades (Produto, Cliente, Venda, ItemVenda, DocumentoFiscal, SyncQueue)
├── repository/     # Repositórios Spring Data JPA
└── service/        # Regras de negócio (Dashboard, Produto, Cliente, Venda, Fiscal, SyncQueue)

src/main/resources/
├── application.properties
└── static/         # Telas (HTML/CSS/JS)
```

---

## Configuração

Principais propriedades em `src/main/resources/application.properties`:

| Propriedade | Padrão | Descrição |
|---|---|---|
| `server.port` | `8080` | Porta do servidor |
| `spring.datasource.url` | `jdbc:h2:file:./data/pdvdb` | Banco H2 local |
| `fiscal.modo` | `simulacao` | `simulacao` gera XML/chave sem enviar à SEFAZ |
| `fiscal.empresa.*` | dados de exemplo | Dados do emitente usados no documento fiscal |
| `sync.server.url` | *(vazio)* | URL do servidor central; vazio = modo totalmente local |
| `sync.enabled` | `true` | Habilita o módulo de sincronização |
| `sync.interval-ms` | `60000` | Intervalo de tentativa de sincronização |

> O console do H2 fica disponível em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/pdvdb`, usuário `sa`, senha em branco).

---

## Observações

- **Modo fiscal:** por padrão o sistema opera em `simulacao`. Para emissão real é necessário configurar certificado digital e integração com a SEFAZ.
- **Offline:** todas as operações são persistidas localmente; a fila de sincronização permite enviar os dados quando a conexão estiver disponível.
