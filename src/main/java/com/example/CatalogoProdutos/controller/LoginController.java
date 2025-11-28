package com.example.CatalogoProdutos.controller;

import com.example.CatalogoProdutos.model.Produto;
import com.example.CatalogoProdutos.model.Usuario;
import com.example.CatalogoProdutos.repository.ProdutoRepository;
import com.example.CatalogoProdutos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String paginaHome(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<Produto> produtos;

        if (keyword != null && !keyword.isEmpty()) {
            produtos = produtoRepository.findByNomeContainingIgnoreCase(keyword);

            for (Produto produto : produtos) {
                if (produto.getPopularidade() == null) {
                    produto.setPopularidade(0);
                }
                produto.setPopularidade(produto.getPopularidade() + 1);
                produtoRepository.save(produto);
            }
        } else {
            produtos = produtoRepository.findAll();
        }

        List<Produto> destaques = produtoRepository.findTop4ByOrderByPopularidadeDesc();
        model.addAttribute("destaques", destaques);
        model.addAttribute("produtos", produtos);
        model.addAttribute("keyword", keyword);

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

    @PostMapping("/salvar-perfil")
    public String salvarPerfil(@ModelAttribute Usuario usuarioForm, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            String emailLogado = principal.getName();
            Usuario usuarioBanco = usuarioRepository.findByEmail(emailLogado)
                    .orElseThrow(() -> new IllegalArgumentException("Erro ao localizar usuário"));

            usuarioBanco.setNome(usuarioForm.getNome());
            usuarioBanco.setTelefone(usuarioForm.getTelefone());
            usuarioBanco.setCpf(usuarioForm.getCpf());

            usuarioRepository.save(usuarioBanco);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil atualizado com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar.");
        }

        return "redirect:/perfil";
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