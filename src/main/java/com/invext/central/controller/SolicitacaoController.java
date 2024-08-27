package com.invext.central.controller;

import com.invext.central.model.Atendente;
import com.invext.central.model.Solicitacao;
import com.invext.central.service.DistribuicaoSolicitacoesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/solicitacoes")
public class SolicitacaoController {

    @Autowired
    private DistribuicaoSolicitacoesService distribuicaoSolicitacoesService;

    @PostMapping
    public String criarSolicitacao(@RequestBody Solicitacao solicitacao) {
        distribuicaoSolicitacoesService.distribuirSolicitacao(solicitacao);
        return "Solicitação recebida e em processamento.";
    }

    @PostMapping("/atendentes/{id}/finalizar")
    public ResponseEntity<String> finalizarAtendimento(@PathVariable String id) {
        distribuicaoSolicitacoesService.finalizarAtendimento(id);
        return ResponseEntity.ok("Atendimento finalizado.");
    }

    @GetMapping("/em-atendimento")
    public Map<String, List<Solicitacao>> obterSolicitacoesEmAtendimento() {
        Map<Atendente, List<Solicitacao>> atendenteSolicitacoesMap = distribuicaoSolicitacoesService.getSolicitacoesEmAtendimento();
        return atendenteSolicitacoesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getNome(),
                        Map.Entry::getValue
                ));
    }

    @GetMapping("/filaDeEspera")
    public List<Solicitacao> getFilaDeEspera() {
        return distribuicaoSolicitacoesService.getFilaDeEspera();
    }
}
