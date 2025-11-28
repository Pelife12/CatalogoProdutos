package com.example.CatalogoProdutos.service;

import com.example.CatalogoProdutos.model.Produto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PdfGenerationService {
    private static final Font FONT_NOME = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_DESC = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
    private static final Font FONT_PRECO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(0xD9, 0x46, 0xEF));
    private static final Font FONT_PLACEHOLDER = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY);

    public void exportarProdutos(HttpServletResponse response, List<Produto> produtos, String uploadDir) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=catalogo_produtos.pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Catálogo de Produtos - Busnardo", fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        for (Produto produto : produtos) {
            PdfPCell card = createProductCard(produto, uploadDir);
            card.setBorder(Rectangle.NO_BORDER);
            card.setPadding(8);

            table.addCell(card);
        }

        if (produtos.size() % 2 != 0) {
            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            emptyCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(emptyCell);
        }

        document.add(table);
        document.close();
    }

    private PdfPCell createProductCard(Produto produto, String uploadDir) throws IOException, BadElementException {
        PdfPTable cardTable = new PdfPTable(1);
        cardTable.setWidthPercentage(100);

        PdfPCell cardContent = new PdfPCell();
        cardContent.setBorder(Rectangle.BOX);
        cardContent.setBorderColor(Color.LIGHT_GRAY);
        cardContent.setPadding(10);
        cardContent.setMinimumHeight(280);
        cardContent.setHorizontalAlignment(Element.ALIGN_CENTER);

        try {
            if (produto.getCaminhoImagem() != null) {
                String nomeImagem = produto.getCaminhoImagem().replace("/imagens/produtos/", "");

                Path path = Paths.get(uploadDir).resolve(nomeImagem);
                String imagePath = path.toAbsolutePath().toString();

                if (Files.exists(path)) {
                    Image img = Image.getInstance(imagePath);
                    img.scaleToFit(140, 140);
                    img.setAlignment(Element.ALIGN_CENTER);
                    cardContent.addElement(img);
                } else {
                    throw new IOException("Arquivo não encontrado: " + imagePath);
                }
            } else {
                throw new Exception("Caminho nulo");
            }
        } catch (Exception e) {
            Paragraph placeholder = new Paragraph("Imagem indisponível", FONT_PLACEHOLDER);
            placeholder.setAlignment(Element.ALIGN_CENTER);
            placeholder.setSpacingBefore(60);
            placeholder.setSpacingAfter(60);
            cardContent.addElement(placeholder);
        }

        Paragraph nome = new Paragraph(produto.getNome(), FONT_NOME);
        nome.setAlignment(Element.ALIGN_LEFT);
        nome.setSpacingBefore(10);
        cardContent.addElement(nome);

        Paragraph desc = new Paragraph(produto.getDescricao(), FONT_DESC);
        desc.setAlignment(Element.ALIGN_LEFT);
        desc.setSpacingAfter(10);
        cardContent.addElement(desc);

        String precoFormatado = String.format("R$ %.2f", produto.getPreco());
        Paragraph preco = new Paragraph(precoFormatado, FONT_PRECO);
        preco.setAlignment(Element.ALIGN_LEFT);
        cardContent.addElement(preco);
        cardTable.addCell(cardContent);

        return new PdfPCell(cardTable);
    }
}