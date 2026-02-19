package ma.entraide.gestionprojet.rapport.service;

import com.lowagie.text.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ma.entraide.gestionprojet.entity.Tache;
import ma.entraide.gestionprojet.rapport.dto.RapportAlertesData;
import ma.entraide.gestionprojet.rapport.util.PdfGeneratorUtil;
import ma.entraide.gestionprojet.repository.TacheRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import static ma.entraide.gestionprojet.rapport.util.PdfGeneratorUtil.*;


@Service
@Transactional(readOnly = true)
public class RapportAlertesService {

    private final TacheRepository tacheRepository;

    public RapportAlertesService(TacheRepository tacheRepository) {
        this.tacheRepository = tacheRepository;
    }

    public byte[] genererRapportAlertes(String generePar, LocalDate dateDebut, LocalDate dateFin) {
        RapportAlertesData data = buildData(dateDebut, dateFin);
        return genererPdf(data, generePar);
    }

    private RapportAlertesData buildData(LocalDate dateDebut, LocalDate dateFin) {
        LocalDate today = LocalDate.now();

        var tachesEnRetard = tacheRepository.findAllTachesEnRetard(today).stream()
                .filter(t -> filterByPeriod(t, dateDebut, dateFin))
                .map(t -> toAlerte(t, t.getJoursRetard()))
                .toList();

        var tachesProches = tacheRepository.findTachesProchesDeadline(today, today.plusDays(3)).stream()
                .filter(t -> filterByPeriod(t, dateDebut, dateFin))
                .map(t -> {
                    long joursRestants = ChronoUnit.DAYS.between(today, t.getDateEcheance());
                    return toAlerte(t, joursRestants);
                })
                .toList();

        long totalTaches = tacheRepository.count();
        long totalEnRetard = tachesEnRetard.size();
        double retardMoyen = tachesEnRetard.stream()
                .mapToLong(RapportAlertesData.TacheAlerte::joursRetardOuRestants)
                .average()
                .orElse(0);
        double tauxRespect = totalTaches > 0
                ? (1 - (double) totalEnRetard / totalTaches) * 100 : 100;

        return new RapportAlertesData(
                totalEnRetard,
                retardMoyen,
                tauxRespect,
                tachesEnRetard,
                tachesProches
        );
    }

    private boolean filterByPeriod(Tache t, LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut != null && t.getDateEcheance() != null && t.getDateEcheance().isBefore(dateDebut))
            return false;
        if (dateFin != null && t.getDateEcheance() != null && t.getDateEcheance().isAfter(dateFin))
            return false;
        return true;
    }

    private RapportAlertesData.TacheAlerte toAlerte(Tache t, long jours) {
        String assignes = t.getAssignees().stream()
                .map(a -> a.getUser().getNomComplet())
                .collect(Collectors.joining(", "));
        return new RapportAlertesData.TacheAlerte(
                t.getProjet().getNom(),
                t.getProjet().getEquipe().getNom(),
                t.getTitre(),
                t.getPriorite().name(),
                assignes.isEmpty() ? "Non assigne" : assignes,
                t.getDateEcheance(),
                jours
        );
    }

    private byte[] genererPdf(RapportAlertesData data, String generePar) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 50);
            document.setFooter(createFooter());
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, "RAPPORT DES ALERTES ET RETARDS", generePar);

            PdfPTable kpiTable = new PdfPTable(3);
            kpiTable.setWidthPercentage(80);
            kpiTable.addCell(kpiCell("Total retards", String.valueOf(data.totalRetards()), DANGER));
            kpiTable.addCell(kpiCell("Retard moyen", String.format("%.1f jours", data.retardMoyenJours()), WARNING));
            kpiTable.addCell(kpiCell("Respect delais", String.format("%.0f%%", data.tauxRespectDelais()), SUCCESS));
            document.add(kpiTable);

            if (!data.tachesEnRetard().isEmpty()) {
                document.add(sectionTitle("TACHES EN RETARD (" + data.tachesEnRetard().size() + ")"));
                addAlertesTable(document, data.tachesEnRetard(), "Jours retard", true);
            }

            if (!data.tachesProchesDeadline().isEmpty()) {
                document.add(sectionTitle("TACHES APPROCHANT LA DEADLINE (" + data.tachesProchesDeadline().size() + ")"));
                addAlertesTable(document, data.tachesProchesDeadline(), "Jours restants", false);
            }

            if (data.tachesEnRetard().isEmpty() && data.tachesProchesDeadline().isEmpty()) {
                Paragraph msg = new Paragraph("Aucune alerte active. Toutes les taches respectent leurs delais.",
                        PdfGeneratorUtil.successFont());
                msg.setSpacingBefore(20);
                msg.setAlignment(Element.ALIGN_CENTER);
                document.add(msg);
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur generation rapport alertes PDF", e);
        }
    }

    private void addAlertesTable(Document document, java.util.List<RapportAlertesData.TacheAlerte> alertes,
                                 String derniereColonne, boolean isDanger) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1.5f, 2.5f, 1.2f, 2, 1.2f, 1});

        table.addCell(headerCell("Projet"));
        table.addCell(headerCell("Equipe"));
        table.addCell(headerCell("Tache"));
        table.addCell(headerCell("Priorite"));
        table.addCell(headerCell("Assignes"));
        table.addCell(headerCell("Echeance"));
        table.addCell(headerCell(derniereColonne));

        for (int i = 0; i < alertes.size(); i++) {
            var a = alertes.get(i);
            PdfPCell projetCell = dataCell(a.projet());
            applyAlternateRowColor(projetCell, i);
            table.addCell(projetCell);

            PdfPCell equipeCell = dataCell(a.equipe());
            applyAlternateRowColor(equipeCell, i);
            table.addCell(equipeCell);

            PdfPCell tacheCell = dataCell(a.tache());
            if (isDanger) tacheCell.setBackgroundColor(new java.awt.Color(254, 226, 226));
            else applyAlternateRowColor(tacheCell, i);
            table.addCell(tacheCell);

            table.addCell(prioriteCell(a.priorite()));

            PdfPCell assignCell = dataCell(a.assignes());
            applyAlternateRowColor(assignCell, i);
            table.addCell(assignCell);

            table.addCell(dataCellCenter(formatDate(a.dateEcheance())));

            Font font = isDanger ? dangerFont() : warningFont();
            PdfPCell joursCell = new PdfPCell(new Phrase(a.joursRetardOuRestants() + "j", font));
            joursCell.setPadding(5);
            joursCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(joursCell);
        }

        document.add(table);
    }
}

