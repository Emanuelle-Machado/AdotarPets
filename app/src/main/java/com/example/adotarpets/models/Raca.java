package com.example.adotarpets.models;

import java.io.Serializable;

public class Raca implements Serializable {
    private int id;
    private String descricao;
    private int idTipo;
    private Tipo tipo;

    public Raca() {}

    public Raca(int id, String descricao, int idTipo, Tipo tipo) {
        this.id = id;
        this.descricao = descricao;
        this.idTipo = idTipo;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return descricao;
    }
}

