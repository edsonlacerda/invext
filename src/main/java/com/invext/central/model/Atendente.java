package com.invext.central.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Atendente {
    private String id;
    private String nome;
    private AtomicInteger atendimentosSimultaneos = new AtomicInteger(0);

    public Atendente(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public boolean podeAtender() {
        return atendimentosSimultaneos.get() < 3;
    }

    public void atender() {
        atendimentosSimultaneos.incrementAndGet();
    }

    public void finalizarAtendimento() {
        atendimentosSimultaneos.updateAndGet(value -> Math.max(0, value - 1));
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public AtomicInteger getAtendimentosSimultaneos() {
        return atendimentosSimultaneos;
    }

    public void setAtendimentosSimultaneos(AtomicInteger atendimentosSimultaneos) {
        this.atendimentosSimultaneos = atendimentosSimultaneos;
    }
}
