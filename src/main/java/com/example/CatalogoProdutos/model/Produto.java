package com.example.CatalogoProdutos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "produtos")
@Getter
@Setter
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private Double preco;

    private Integer desconto;

    @Column(name = "caminho_imagem")
    private String caminhoImagem;

    private Integer popularidade = 0;

    public Double getPrecoComDesconto() {
        if (this.desconto == null || this.desconto <= 0) {
            return this.preco;
        }
        return this.preco - (this.preco * this.desconto / 100.0);
    }
}