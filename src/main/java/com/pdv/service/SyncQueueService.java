package com.pdv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdv.model.SyncQueue;
import com.pdv.model.Venda;
import com.pdv.repository.SyncQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SyncQueueService {

    @Autowired
    private SyncQueueRepository syncQueueRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${sync.server.url:}")
    private String syncServerUrl;

    @Value("${sync.enabled:true}")
    private boolean syncEnabled;

    private LocalDateTime ultimaSincronizacao;

    /**
     * Registra uma venda na fila para sincronizacao posterior.
     */
    public void registrarVenda(Venda venda) {
        try {
            SyncQueue sq = new SyncQueue();
            sq.setEntidade("VENDA");
            sq.setEntidadeId(venda.getId());
            sq.setOperacao("CREATE");
            sq.setDadosJson(objectMapper.writeValueAsString(venda));
            sq.setStatus("PENDENTE");
            syncQueueRepository.save(sq);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao registrar venda na fila de sync", e);
        }
    }

    /**
     * Registra qualquer entidade na fila de sincronizacao.
     */
    public void registrar(String entidade, Long entidadeId, String operacao, Object dados) {
        try {
            SyncQueue sq = new SyncQueue();
            sq.setEntidade(entidade);
            sq.setEntidadeId(entidadeId);
            sq.setOperacao(operacao);
            sq.setDadosJson(objectMapper.writeValueAsString(dados));
            sq.setStatus("PENDENTE");
            syncQueueRepository.save(sq);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao registrar na fila de sync", e);
        }
    }

    public List<SyncQueue> listarPendentes() {
        return syncQueueRepository.findByStatusOrderByDataCriacaoAsc("PENDENTE");
    }

    public long contarPendentes() {
        return syncQueueRepository.countByStatus("PENDENTE");
    }

    public LocalDateTime ultimaSincronizacao() {
        return ultimaSincronizacao;
    }

    /**
     * Processa a fila de sincronizacao.
     * Se houver servidor configurado e conexao, envia os dados.
     * Caso contrario, mantem como pendente (offline).
     */
    @Scheduled(fixedDelayString = "${sync.interval-ms:60000}")
    @Transactional
    public void sincronizar() {
        if (!syncEnabled || syncServerUrl == null || syncServerUrl.isEmpty()) {
            return;
        }

        List<SyncQueue> pendentes = listarPendentes();
        if (pendentes.isEmpty()) {
            ultimaSincronizacao = LocalDateTime.now();
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (SyncQueue sq : pendentes) {
            try {
                HttpEntity<String> request = new HttpEntity<>(sq.getDadosJson(), headers);
                String url = syncServerUrl + "/sync/" + sq.getEntidade().toLowerCase();
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    sq.setStatus("ENVIADO");
                    sq.setDataEnvio(LocalDateTime.now());
                    syncQueueRepository.save(sq);
                } else {
                    sq.setTentativas(sq.getTentativas() + 1);
                    sq.setMensagemErro("HTTP " + response.getStatusCode());
                    if (sq.getTentativas() >= 5) {
                        sq.setStatus("ERRO");
                    }
                    syncQueueRepository.save(sq);
                }
            } catch (Exception e) {
                // Sem conexao - mantem pendente para tentar depois
                sq.setTentativas(sq.getTentativas() + 1);
                sq.setMensagemErro(e.getMessage());
                if (sq.getTentativas() >= 5) {
                    sq.setStatus("ERRO");
                }
                syncQueueRepository.save(sq);
            }
        }

        ultimaSincronizacao = LocalDateTime.now();
    }

    /**
     * Sincronizacao manual (acionada pelo usuario no painel).
     */
    public String sincronizarManual() {
        long pendentes = contarPendentes();
        if (syncServerUrl == null || syncServerUrl.isEmpty()) {
            return "Servidor de sincronizacao nao configurado. " + pendentes +
                    " registro(s) aguardando.";
        }
        sincronizar();
        return "Sincronizacao iniciada. " + pendentes + " registro(s) processado(s).";
    }

    public void limparEnviados() {
        List<SyncQueue> enviados = syncQueueRepository.findByStatusOrderByDataCriacaoAsc("ENVIADO");
        syncQueueRepository.deleteAll(enviados);
    }

    public Optional<SyncQueue> buscarPorId(Long id) {
        return syncQueueRepository.findById(id);
    }
}
