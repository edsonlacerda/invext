package com.invext.central.service;

import com.invext.central.model.Atendente;
import com.invext.central.model.Solicitacao;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DistribuicaoSolicitacoesService {
    private final List<Atendente> atendentesCartoes;
    private final List<Atendente> atendentesEmprestimos;
    private final List<Atendente> atendentesOutros;
    private final RabbitTemplate rabbitTemplate;
    private final Queue<Solicitacao> filaDeEspera = new LinkedList<>();

    private final Map<Atendente, List<Solicitacao>> atendenteSolicitacoesMap = new ConcurrentHashMap<>();

    public DistribuicaoSolicitacoesService(List<Atendente> atendentesCartoes, List<Atendente> atendentesEmprestimos, List<Atendente> atendentesOutros, RabbitTemplate rabbitTemplate) {
        this.atendentesCartoes = atendentesCartoes;
        this.atendentesEmprestimos = atendentesEmprestimos;
        this.atendentesOutros = atendentesOutros;
        this.rabbitTemplate = rabbitTemplate;

        initAtendentesSolicitacoesMap();
    }

    private void initAtendentesSolicitacoesMap() {
        for (Atendente atendente : atendentesCartoes) {
            atendenteSolicitacoesMap.put(atendente, new ArrayList<>());
        }
        for (Atendente atendente : atendentesEmprestimos) {
            atendenteSolicitacoesMap.put(atendente, new ArrayList<>());
        }
        for (Atendente atendente : atendentesOutros) {
            atendenteSolicitacoesMap.put(atendente, new ArrayList<>());
        }
    }

    @RabbitListener(queues = "solicitacaoQueue")
    public void distribuirSolicitacao(Solicitacao solicitacao) {
        List<Atendente> timeResponsavel = getAtendentesPorTipo(solicitacao.getTipo());

        Optional<Atendente> atendenteDisponivel = timeResponsavel.stream()
                .filter(Atendente::podeAtender)
                .findFirst();

        if (atendenteDisponivel.isPresent()) {
            atendenteDisponivel.get().atender();
            atendenteSolicitacoesMap.get(atendenteDisponivel.get()).add(solicitacao);
        } else {
            rabbitTemplate.convertAndSend("filaDeEsperaQueue", solicitacao);
        }
    }

    private List<Atendente> getAtendentesPorTipo(String tipo) {
        return switch (tipo) {
            case "Problemas com cartão" -> atendentesCartoes;
            case "Contratação de empréstimo" -> atendentesEmprestimos;
            default -> atendentesOutros;
        };
    }

    public void finalizarAtendimento(String idAtendente) {
        Atendente atendente = getAtendenteById(idAtendente);
        if (atendente != null) {
            atendente.finalizarAtendimento();
            processarFilaDeEspera();
            if (atendenteSolicitacoesMap.containsKey(atendente)) {
                List<Solicitacao> solicitacoes = atendenteSolicitacoesMap.get(atendente);
                if (!solicitacoes.isEmpty()) {
                    solicitacoes.remove(0);
                }
            }
        }
    }

    private void processarFilaDeEspera() {
        Iterator<Solicitacao> iterator = filaDeEspera.iterator();

        while (iterator.hasNext()) {
            Solicitacao solicitacao = iterator.next();
            List<Atendente> timeResponsavel = getAtendentesPorTipo(solicitacao.getTipo());
            Optional<Atendente> atendenteDisponivel = timeResponsavel.stream()
                    .filter(Atendente::podeAtender)
                    .findFirst();

            if (atendenteDisponivel.isPresent()) {
                Atendente atendente = atendenteDisponivel.get();
                atendente.atender();
                atendenteSolicitacoesMap.get(atendente).add(solicitacao);
                iterator.remove();
                break;
            }
        }
    }

    @RabbitListener(queues = "filaDeEsperaQueue")
    public void adicionarAFilaDeEspera(Solicitacao solicitacao) {
        filaDeEspera.add(solicitacao);
    }

    private Atendente getAtendenteById(String id) {
        return atendentesCartoes.stream().filter(a -> a.getId().equals(id)).findFirst()
                .or(() -> atendentesEmprestimos.stream().filter(a -> a.getId().equals(id)).findFirst())
                .or(() -> atendentesOutros.stream().filter(a -> a.getId().equals(id)).findFirst())
                .orElse(null);
    }

    public Map<Atendente, List<Solicitacao>> getSolicitacoesEmAtendimento() {
        return atendenteSolicitacoesMap;
    }

    public List<Solicitacao> getFilaDeEspera() {
        return new ArrayList<>(filaDeEspera);
    }
}
