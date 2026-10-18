package com.pdv.controller;

import com.pdv.model.Cliente;
import com.pdv.service.ClienteService;
import com.pdv.service.SyncQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private SyncQueueService syncQueueService;

    @GetMapping
    public List<Cliente> listarTodos() {
        return clienteService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Cliente> buscarPorCpf(@PathVariable String cpf) {
        return clienteService.buscarPorCpfCnpj(cpf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Cliente> buscarPorNome(@RequestParam String nome) {
        return clienteService.buscarPorNome(nome);
    }

    @PostMapping
    public Cliente criar(@RequestBody Cliente cliente) {
        Cliente salvo = clienteService.salvar(cliente);
        syncQueueService.registrar("CLIENTE", salvo.getId(), "CREATE", salvo);
        return salvo;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable Long id, @RequestBody Cliente cliente) {
        return clienteService.buscarPorId(id).map(existente -> {
            cliente.setId(id);
            Cliente atualizado = clienteService.salvar(cliente);
            syncQueueService.registrar("CLIENTE", id, "UPDATE", atualizado);
            return ResponseEntity.ok(atualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        syncQueueService.registrar("CLIENTE", id, "DELETE", null);
        return ResponseEntity.noContent().build();
    }
}
