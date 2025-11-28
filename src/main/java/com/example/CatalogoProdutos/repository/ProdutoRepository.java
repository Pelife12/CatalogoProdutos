package com.example.CatalogoProdutos.repository;

import com.example.CatalogoProdutos.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Método para buscar produtos que contenham o texto pesquisado no nome, ignorando maiúsculas/minúsculas
    List<Produto> findByNomeContainingIgnoreCase(String nome);
}