package ma.entraide.gestionprojet.rapport.util;

import com.lowagie.text.*;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitaires communs pour la generation de PDFs.
 * Fournit en-tetes, pieds de page, polices et couleurs standardisees.
 */
public final class PdfGeneratorUtil {

    private PdfGeneratorUtil() {}

    // Couleurs
    public static final Color PRIMARY = new Color(30, 58, 138);       // bleu fonce
    public static final Color SECONDARY = new Color(71, 85, 105);     // gris bleu
    public static final Color LIGHT_BG = new Color(241, 245, 249);    // gris clair
    public static final Color WHITE = Color.WHITE;
    public static final Color DANGER = new Color(220, 38, 38);        // rouge
    public static final Color WARNING = new Color(234, 179, 8);       // jaune/orange
    public static final Color SUCCESS = new Color(22, 163, 74);       // vert
    public static final Color HEADER_BG = new Color(30, 58, 138);     // bleu fonce
    public static final Color ROW_ALT = new Color(248, 250, 252);     // gris tres clair

    // Polices
    public static Font titleFont() {
        return new Font(Font.HELVETICA, 20, Font.BOLD, PRIMARY);
    }

    public static Font subtitleFont() {
        return new Font(Font.HELVETICA, 14, Font.BOLD, SECONDARY);
    }

    public static Font sectionFont() {
        return new Font(Font.HELVETICA, 12, Font.BOLD, PRIMARY);
    }

    public static Font headerCellFont() {
        return new Font(Font.HELVETICA, 9, Font.BOLD, WHITE);
    }

    public static Font cellFont() {
        return new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    }

    public static Font cellBoldFont() {
        return new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
    }

    public static Font smallFont() {
        return new Font(Font.HELVETICA, 8, Font.NORMAL, SECONDARY);
    }

    public static Font labelFont() {
        return new Font(Font.HELVETICA, 10, Font.BOLD, SECONDARY);
    }

    public static Font valueFont() {
        return new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    }

    public static Font dangerFont() {
        return new Font(Font.HELVETICA, 9, Font.BOLD, DANGER);
    }

    public static Font warningFont() {
        return new Font(Font.HELVETICA, 9, Font.BOLD, WARNING);
    }

    public static Font successFont() {
        return new Font(Font.HELVETICA, 9, Font.BOLD, SUCCESS);
    }

    // Formatters
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "-";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FMT) : "-";
    }

    /**
     * Cree l'en-tete du rapport avec titre et meta-informations.
     */
    public static void addHeader(Document document, String title, String generePar) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{3, 2});

        // Titre
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(PdfPCell.NO_BORDER);
        titleCell.setPadding(10);
        Paragraph titlePara = new Paragraph(title, titleFont());
        titleCell.addElement(titlePara);

        // Meta
        PdfPCell metaCell = new PdfPCell();
        metaCell.setBorder(PdfPCell.NO_BORDER);
        metaCell.setPadding(10);
        metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph meta = new Paragraph();
        meta.setAlignment(Element.ALIGN_RIGHT);
        meta.add(new Chunk("Genere le : " + formatDateTime(LocalDateTime.now()) + "\n", smallFont()));
        meta.add(new Chunk("Par : " + generePar, smallFont()));
        metaCell.addElement(meta);

        headerTable.addCell(titleCell);
        headerTable.addCell(metaCell);

        document.add(headerTable);

        // Ligne de separation
        PdfPTable separator = new PdfPTable(1);
        separator.setWidthPercentage(100);
        PdfPCell sepCell = new PdfPCell();
        sepCell.setBorder(PdfPCell.BOTTOM);
        sepCell.setBorderColor(PRIMARY);
        sepCell.setBorderWidth(2);
        sepCell.setFixedHeight(5);
        separator.addCell(sepCell);
        document.add(separator);
        document.add(new Paragraph(" "));
    }

    /**
     * Cree un bloc de label + valeur pour les fiches.
     */
    public static PdfPCell labelValueCell(String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(4);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " : ", labelFont()));
        p.add(new Chunk(value, valueFont()));
        cell.addElement(p);
        return cell;
    }

    /**
     * Cree un en-tete de tableau stylise.
     */
    public static PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, headerCellFont()));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    /**
     * Cree une cellule de donnees.
     */
    public static PdfPCell dataCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, cellFont()));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    /**
     * Cree une cellule de donnees centree.
     */
    public static PdfPCell dataCellCenter(String text) {
        PdfPCell cell = dataCell(text);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    /**
     * Cree une cellule avec indicateur de priorite.
     */
    public static PdfPCell prioriteCell(String priorite) {
        Color color = switch (priorite) {
            case "CRITIQUE" -> DANGER;
            case "HAUTE" -> WARNING;
            case "MOYENNE" -> new Color(59, 130, 246); // bleu
            default -> SUCCESS;
        };
        Font font = new Font(Font.HELVETICA, 9, Font.BOLD, color);
        PdfPCell cell = new PdfPCell(new Phrase(priorite, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    /**
     * Cree une cellule avec indicateur de statut.
     */
    public static PdfPCell statutCell(String statut) {
        Color color = switch (statut) {
            case "TERMINEE", "TERMINE" -> SUCCESS;
            case "EN_COURS" -> new Color(59, 130, 246);
            case "EN_RETARD", "BLOQUEE" -> DANGER;
            case "EN_PAUSE", "EN_REVUE" -> WARNING;
            default -> SECONDARY;
        };
        Font font = new Font(Font.HELVETICA, 9, Font.BOLD, color);
        PdfPCell cell = new PdfPCell(new Phrase(statut.replace("_", " "), font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    /**
     * Applique un fond alterne aux lignes d'un tableau.
     */
    public static void applyAlternateRowColor(PdfPCell cell, int rowIndex) {
        if (rowIndex % 2 == 0) {
            cell.setBackgroundColor(ROW_ALT);
        } else {
            cell.setBackgroundColor(WHITE);
        }
    }

    /**
     * Cree un titre de section.
     */
    public static Paragraph sectionTitle(String title) {
        Paragraph p = new Paragraph(title, sectionFont());
        p.setSpacingBefore(15);
        p.setSpacingAfter(8);
        return p;
    }

    /**
     * Cree un bloc KPI (label + valeur grande).
     */
    public static PdfPCell kpiCell(String label, String value, Color valueColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.BOX);
        cell.setBorderColor(LIGHT_BG);
        cell.setBackgroundColor(LIGHT_BG);
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph valuePara = new Paragraph(value, new Font(Font.HELVETICA, 24, Font.BOLD, valueColor));
        valuePara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(valuePara);

        Paragraph labelPara = new Paragraph(label, smallFont());
        labelPara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(labelPara);

        return cell;
    }

    /**
     * Barre de progression textuelle.
     */
    public static String progressBar(int percentage) {
        int filled = percentage / 10;
        int empty = 10 - filled;
        return "|".repeat(filled) + " ".repeat(empty) + " " + percentage + "%";
    }

    /**
     * Genere un pied de page avec numero de page.
     */
    public static HeaderFooter createFooter() {
        Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, SECONDARY);
        Phrase footerPhrase = new Phrase("Confidentiel - Gestion de Projets - " + formatDate(LocalDate.now()), footerFont);
        HeaderFooter footer = new HeaderFooter(footerPhrase, true);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setBorder(HeaderFooter.TOP);
        footer.setBorderColor(LIGHT_BG);
        return footer;
    }
}

