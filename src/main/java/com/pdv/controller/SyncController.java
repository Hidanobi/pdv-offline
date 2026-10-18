package com.pdv.controller;

import com.pdv.model.SyncQueue;
import com.pdv.service.SyncQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    @Autowired
    private SyncQueueService syncQueueService;

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> status = new HashMap<>();
        status.put("pendentes", syncQueueService.contarPendentes());
        status.put("ultimaSincronizacao", syncQueueService.ultimaSincronizacao());
        return status;
    }

    @GetMapping("/pendentes")
    public List<SyncQueue> pendentes() {
        return syncQueueService.listarPendentes();
    }

    @PostMapping("/sincronizar")
    public Map<String, String> sincronizar() {
        Map<String, String> result = new HashMap<>();
        result.put("mensagem", syncQueueService.sincronizarManual());
        return result;
    }

    @DeleteMapping("/limpar")
    public Map<String, String> limparEnviados() {
        syncQueueService.limparEnviados();
        Map<String, String> result = new HashMap<>();
        result.put("mensagem", "Registros enviados removidos.");
        return result;
    }
}
