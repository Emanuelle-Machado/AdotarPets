package com.example.adotarpets.models;

import java.io.Serializable;

public class Animal implements Serializable {
    private int id;
    private String descricao;
    private String cor;
    private int idade;
    private double valor;
    private String finalidade; // "D" ou "A"
    private String nomeProprietario;
    private String contato;

    private int idCidade;
    private int idRaca;
    private Cidade cidade;
    private Raca raca;

    public Animal() {}

    public Animal(int id, String descricao, String cor, int idade, double valor, String finalidade,
                  String nomeProprietario, String contato, int idCidade, int idRaca,
                  Cidade cidade, Raca raca) {
        this.id = id;
        this.descricao = descricao;
        this.cor = cor;
        this.idade = idade;
        this.valor = valor;
        this.finalidade = finalidade;
        this.nomeProprietario = nomeProprietario;
        this.contato = contato;
        this.idCidade = idCidade;
        this.idRaca = idRaca;
        this.cidade = cidade;
        this.raca = raca;
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

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getFinalidade() {
        return finalidade;
    }

    public void setFinalidade(String finalidade) {
        this.finalidade = finalidade;
    }

    public String getNomeProprietario() {
        return nomeProprietario;
    }

    public void setNomeProprietario(String nomeProprietario) {
        this.nomeProprietario = nomeProprietario;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public int getIdCidade() {
        return idCidade;
    }

    public void setIdCidade(int idCidade) {
        this.idCidade = idCidade;
    }

    public int getIdRaca() {
        return idRaca;
    }

    public void setIdRaca(int idRaca) {
        this.idRaca = idRaca;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public void setCidade(Cidade cidade) {
        this.cidade = cidade;
    }

    public Raca getRaca() {
        return raca;
    }

    public void setRaca(Raca raca) {
        this.raca = raca;
    }

    @Override
    public String toString() {
        return descricao + " (" + cor + ", " + idade + " meses)";
    }
}
