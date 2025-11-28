package com.example.CatalogoProdutos.controller;

import com.example.CatalogoProdutos.model.Produto;
import com.example.CatalogoProdutos.repository.ProdutoRepository;
import com.example.CatalogoProdutos.service.PdfGenerationService;
import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
public class ProdutoController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    @GetMapping("/cadastro-produto")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("produto", new Produto());
        model.addAttribute("pageTitle", "Cadastro de Produto");
        model.addAttribute("formAction", "/salvar-produto");
        return "cadastroProduto";
    }

    @GetMapping("/editar-produto/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Produto produto = produtoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Produto inválido:" + id));

            model.addAttribute("produto", produto);
            model.addAttribute("pageTitle", "Editar Produto");
            model.addAttribute("formAction", "/salvar-produto");
            return "cadastroProduto";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Produto não encontrado.");
            return "redirect:/index";
        }
    }

    @PostMapping("/salvar-produto")
    public String salvarProduto(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("nome") String nome,
            @RequestParam("descricao") String descricao,
            @RequestParam("preco") Double preco,
            @RequestParam(value = "desconto", required = false) Integer desconto,
            @RequestParam("imagem") MultipartFile imagem,
            RedirectAttributes redirectAttributes) {

        try {
            Produto produto;
            String successMessage;

            if (id == null) {
                produto = new Produto();
                successMessage = "Produto cadastrado com sucesso!";
                if (imagem.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "A imagem do produto é obrigatória.");
                    return "redirect:/cadastro-produto";
                }
            } else {
                produto = produtoRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Produto inválido:" + id));
                successMessage = "Produto atualizado com sucesso!";
            }

            produto.setNome(nome);
            produto.setDescricao(descricao);
            produto.setPreco(preco);
            produto.setDesconto(desconto != null ? desconto : 0);

            if (!imagem.isEmpty()) {
                if (id != null && produto.getCaminhoImagem() != null && !produto.getCaminhoImagem().isEmpty()) {
                    String nomeImagemAntiga = produto.getCaminhoImagem().replace("/imagens/produtos/", "");
                    Path caminhoAntigo = Paths.get(uploadDir).resolve(nomeImagemAntiga);
                    Files.deleteIfExists(caminhoAntigo);
                }

                String nomeArquivoUnico = UUID.randomUUID().toString() + "_" + imagem.getOriginalFilename();

                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path caminhoArquivo = uploadPath.resolve(nomeArquivoUnico);
                Files.write(caminhoArquivo, imagem.getBytes());

                produto.setCaminhoImagem("/imagens/produtos/" + nomeArquivoUnico);
            }

            produtoRepository.save(produto);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/index";

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar a imagem.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar o produto.");
        }

        if (id == null) {
            return "redirect:/cadastro-produto";
        } else {
            return "redirect:/editar-produto/" + id;
        }
    }


    @PostMapping("/excluir-produto/{id}")
    public String excluirProduto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Produto produto = produtoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

            if (produto.getCaminhoImagem() != null && !produto.getCaminhoImagem().isEmpty()) {
                String nomeImagem = produto.getCaminhoImagem().replace("/imagens/produtos/", "");
                Path caminhoImagem = Paths.get(uploadDir).resolve(nomeImagem);
                Files.deleteIfExists(caminhoImagem);
            }

            produtoRepository.delete(produto);
            redirectAttributes.addFlashAttribute("successMessage", "Produto excluído com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao excluir o produto.");
        }

        return "redirect:/index";
    }

    @GetMapping("/exportar/pdf")
    public void exportarPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Produto> produtos = produtoRepository.findAll();
        pdfGenerationService.exportarProdutos(response, produtos, uploadDir);
    }
}