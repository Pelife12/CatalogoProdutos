package com.example.CatalogoProdutos.controller;

import com.example.CatalogoProdutos.model.Produto;
import com.example.CatalogoProdutos.model.Usuario;
import com.example.CatalogoProdutos.repository.ProdutoRepository;
import com.example.CatalogoProdutos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/login")
    public String exibirTelaDeLogin() {
        return "login";
    }

    @GetMapping("/index")
    public String paginaHome(Model model) {
        List<Produto> produtos = produtoRepository.findAll();

        model.addAttribute("produtos", produtos);

        return "index";
    }

    @GetMapping("/perfil")
    public String paginaPerfil(Model model, Principal principal) {
        String emailUsuario = principal.getName();

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + emailUsuario));

        model.addAttribute("usuario", usuario);

        return "perfil";
    }

    @GetMapping("/")
    public String redirecionarParaIndex() {
        return "redirect:/index";
    }

    @GetMapping("/sobre")
    public String paginaSaibaMais() {
        return "saiba_mais";
    }
}