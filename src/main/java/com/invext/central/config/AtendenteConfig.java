package com.invext.central.config;

import com.invext.central.model.Atendente;
import com.invext.central.service.DistribuicaoSolicitacoesService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AtendenteConfig {

    @Bean
    public DistribuicaoSolicitacoesService distribuicaoSolicitacoesService(RabbitTemplate rabbitTemplate) {
        List<Atendente> atendentesCartoes = List.of(new Atendente("1", "João"), new Atendente("2", "Maria"));
        List<Atendente> atendentesEmprestimos = List.of(new Atendente("3", "Ana"), new Atendente("4", "Carlos"));
        List<Atendente> atendentesOutros = List.of(new Atendente("5", "Pedro"), new Atendente("6", "Luiza"));

        return new DistribuicaoSolicitacoesService(atendentesCartoes, atendentesEmprestimos, atendentesOutros, rabbitTemplate);
    }
}
