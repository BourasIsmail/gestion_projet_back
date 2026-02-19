package ma.entraide.gestionprojet.rapport.service;

import com.lowagie.text.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ma.entraide.gestionprojet.entity.Projet;
import ma.entraide.gestionprojet.entity.enums.StatutTache;
import ma.entraide.gestionprojet.exception.ResourceNotFoundException;
import ma.entraide.gestionprojet.rapport.dto.RapportProjetData;
import ma.entraide.gestionprojet.repository.ProjetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import static ma.entraide.gestionprojet.rapport.util.PdfGeneratorUtil.*;

@Service
@Transactional(readOnly = true)
public class RapportProjetService {

    private final ProjetRepository projetRepository;

    public RapportProjetService(ProjetRepository projetRepository) {
        this.projetRepository = projetRepository;
    }

    public byte[] genererRapportProjet(Long projetId, String generePar) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouve : " + projetId));

        RapportProjetData data = buildData(projet);
        return genererPdf(data, generePar);
    }

    public byte[] genererRapportTaches(Long projetId, String generePar,
                                       LocalDate dateDebut, LocalDate dateFin) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouve : " + projetId));

        RapportProjetData data = buildData(projet);
        return genererPdfTaches(data, generePar, dateDebut, dateFin);
    }

    private RapportProjetData buildData(Projet projet) {
        var membres = projet.getMembres().stream()
                .map(pm -> new RapportProjetData.MembreInfo(
                        pm.getUser().getNomComplet(),
                        pm.getRoleProjet().name()))
                .toList();

        var taches = projet.getTaches().stream()
                .map(t -> {
                    String assignes = t.getAssignees().stream()
                            .map(a -> a.getUser().getNomComplet())
                            .collect(Collectors.joining(", "));
                    boolean enRetard = t.isEnRetard();
                    boolean procheDeadline = !enRetard
                            && t.getDateEcheance() != null
                            && t.getStatut() != StatutTache.TERMINEE
                            && ChronoUnit.DAYS.between(LocalDate.now(), t.getDateEcheance()) <= 3
                            && ChronoUnit.DAYS.between(LocalDate.now(), t.getDateEcheance()) >= 0;

                    return new RapportProjetData.TacheInfo(
                            t.getTitre(),
                            t.getPriorite().name(),
                            t.getStatut().name(),
                            assignes.isEmpty() ? "Non assigne" : assignes,
                            t.getDateEcheance(),
                            t.getPourcentage(),
                            t.getJoursRetard(),
                            enRetard,
                            procheDeadline
                    );
                })
                .toList();

        long terminees = taches.stream().filter(t -> "TERMINEE".equals(t.statut())).count();
        long enCours = taches.stream().filter(t -> "EN_COURS".equals(t.statut())).count();
        long enRetard = taches.stream().filter(RapportProjetData.TacheInfo::enRetard).count();
        long aFaire = taches.stream().filter(t -> "A_FAIRE".equals(t.statut())).count();

        return new RapportProjetData(
                projet.getNom(),
                projet.getDescription(),
                projet.getTypeProjet().getLibelle(),
                projet.getEquipe().getNom(),
                projet.getPriorite().name(),
                projet.getStatut().name(),
                projet.getDateDebut(),
                projet.getDateFinPrevue(),
                projet.getDateFinReelle(),
                projet.getPourcentageProgression(),
                projet.getCreatedBy().getNomComplet(),
                membres,
                taches,
                taches.size(),
                terminees,
                enCours,
                enRetard,
                aFaire
        );
    }

    private byte[] genererPdf(RapportProjetData data, String generePar) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 50);
            document.setFooter(createFooter());
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, "RAPPORT DE PROJET", generePar);

            document.add(sectionTitle("INFORMATIONS DU PROJET"));
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 1});

            infoTable.addCell(labelValueCell("Nom", data.nomProjet()));
            infoTable.addCell(labelValueCell("Type", data.typeProjet()));
            infoTable.addCell(labelValueCell("Equipe", data.equipe()));
            infoTable.addCell(labelValueCell("Priorite", data.priorite()));
            infoTable.addCell(labelValueCell("Statut", data.statut().replace("_", " ")));
            infoTable.addCell(labelValueCell("Cree par", data.creePar()));
            infoTable.addCell(labelValueCell("Date debut", formatDate(data.dateDebut())));
            infoTable.addCell(labelValueCell("Deadline", formatDate(data.dateFinPrevue())));
            infoTable.addCell(labelValueCell("Progression", data.pourcentageProgression() + "%"));
            infoTable.addCell(labelValueCell("Date fin reelle", formatDate(data.dateFinReelle())));

            document.add(infoTable);

            document.add(sectionTitle("INDICATEURS"));
            PdfPTable kpiTable = new PdfPTable(5);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingBefore(5);
            kpiTable.addCell(kpiCell("Total", String.valueOf(data.totalTaches()), PRIMARY));
            kpiTable.addCell(kpiCell("Terminees", String.valueOf(data.tachesTerminees()), SUCCESS));
            kpiTable.addCell(kpiCell("En cours", String.valueOf(data.tachesEnCours()), new java.awt.Color(59, 130, 246)));
            kpiTable.addCell(kpiCell("A faire", String.valueOf(data.tachesAFaire()), SECONDARY));
            kpiTable.addCell(kpiCell("En retard", String.valueOf(data.tachesEnRetard()), DANGER));
            document.add(kpiTable);

            document.add(sectionTitle("MEMBRES DU PROJET"));
            PdfPTable membreTable = new PdfPTable(2);
            membreTable.setWidthPercentage(100);
            membreTable.setWidths(new float[]{3, 2});
            membreTable.addCell(headerCell("Nom"));
            membreTable.addCell(headerCell("Role"));

            for (int i = 0; i < data.membres().size(); i++) {
                var m = data.membres().get(i);
                PdfPCell nomCell = dataCell(m.nom());
                PdfPCell roleCell = dataCellCenter(m.role().replace("_", " "));
                applyAlternateRowColor(nomCell, i);
                applyAlternateRowColor(roleCell, i);
                membreTable.addCell(nomCell);
                membreTable.addCell(roleCell);
            }
            document.add(membreTable);

            addTachesTable(document, data);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la generation du rapport projet PDF", e);
        }
    }

    private byte[] genererPdfTaches(RapportProjetData data, String generePar,
                                    LocalDate filtreDebut, LocalDate filtreFin) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 50);
            document.setFooter(createFooter());
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, "RAPPORT DES TACHES - " + data.nomProjet(), generePar);

            PdfPTable resume = new PdfPTable(4);
            resume.setWidthPercentage(100);
            resume.addCell(labelValueCell("Type", data.typeProjet()));
            resume.addCell(labelValueCell("Statut", data.statut().replace("_", " ")));
            resume.addCell(labelValueCell("Progression", data.pourcentageProgression() + "%"));
            resume.addCell(labelValueCell("Deadline", formatDate(data.dateFinPrevue())));
            document.add(resume);
            document.add(new Paragraph(" "));

            var tachesFiltrees = data.taches().stream()
                    .filter(t -> {
                        if (filtreDebut != null && t.dateEcheance() != null
                                && t.dateEcheance().isBefore(filtreDebut)) return false;
                        if (filtreFin != null && t.dateEcheance() != null
                                && t.dateEcheance().isAfter(filtreFin)) return false;
                        return true;
                    })
                    .toList();

            long terminees = tachesFiltrees.stream().filter(t -> "TERMINEE".equals(t.statut())).count();
            long enRetard = tachesFiltrees.stream().filter(RapportProjetData.TacheInfo::enRetard).count();

            PdfPTable kpiTable = new PdfPTable(3);
            kpiTable.setWidthPercentage(60);
            kpiTable.addCell(kpiCell("Total taches", String.valueOf(tachesFiltrees.size()), PRIMARY));
            kpiTable.addCell(kpiCell("Completees", String.valueOf(terminees), SUCCESS));
            kpiTable.addCell(kpiCell("En retard", String.valueOf(enRetard), DANGER));
            document.add(kpiTable);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1.2f, 1.2f, 2, 1.2f, 1, 1});

            table.addCell(headerCell("Tache"));
            table.addCell(headerCell("Priorite"));
            table.addCell(headerCell("Statut"));
            table.addCell(headerCell("Assignes"));
            table.addCell(headerCell("Echeance"));
            table.addCell(headerCell("Avanc."));
            table.addCell(headerCell("Retard"));

            for (int i = 0; i < tachesFiltrees.size(); i++) {
                var t = tachesFiltrees.get(i);
                PdfPCell titreCell = dataCell(t.titre());
                if (t.enRetard()) titreCell.setBackgroundColor(new java.awt.Color(254, 226, 226));
                else if (t.procheDeadline()) titreCell.setBackgroundColor(new java.awt.Color(254, 249, 195));
                else applyAlternateRowColor(titreCell, i);

                table.addCell(titreCell);
                table.addCell(prioriteCell(t.priorite()));
                table.addCell(statutCell(t.statut()));
                PdfPCell assignCell = dataCell(t.assignes());
                applyAlternateRowColor(assignCell, i);
                table.addCell(assignCell);
                table.addCell(dataCellCenter(formatDate(t.dateEcheance())));
                table.addCell(dataCellCenter(t.pourcentage() + "%"));

                if (t.enRetard()) {
                    table.addCell(new PdfPCell(new Phrase(t.joursRetard() + "j", dangerFont())));
                } else {
                    table.addCell(dataCellCenter("-"));
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur generation rapport taches PDF", e);
        }
    }

    private void addTachesTable(Document document, RapportProjetData data) throws DocumentException {
        document.add(sectionTitle("TACHES DU PROJET"));

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1.2f, 1.2f, 2, 1.2f, 1, 1});

        table.addCell(headerCell("Tache"));
        table.addCell(headerCell("Priorite"));
        table.addCell(headerCell("Statut"));
        table.addCell(headerCell("Assignes"));
        table.addCell(headerCell("Echeance"));
        table.addCell(headerCell("Avanc."));
        table.addCell(headerCell("Retard"));

        for (int i = 0; i < data.taches().size(); i++) {
            var t = data.taches().get(i);
            PdfPCell titreCell = dataCell(t.titre());
            if (t.enRetard()) {
                titreCell.setBackgroundColor(new java.awt.Color(254, 226, 226));
            } else if (t.procheDeadline()) {
                titreCell.setBackgroundColor(new java.awt.Color(254, 249, 195));
            } else {
                applyAlternateRowColor(titreCell, i);
            }
            table.addCell(titreCell);
            table.addCell(prioriteCell(t.priorite()));
            table.addCell(statutCell(t.statut()));

            PdfPCell assignCell = dataCell(t.assignes());
            applyAlternateRowColor(assignCell, i);
            table.addCell(assignCell);
            table.addCell(dataCellCenter(formatDate(t.dateEcheance())));
            table.addCell(dataCellCenter(t.pourcentage() + "%"));

            if (t.enRetard()) {
                PdfPCell retardCell = new PdfPCell(new Phrase(t.joursRetard() + "j", dangerFont()));
                retardCell.setPadding(5);
                retardCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(retardCell);
            } else {
                table.addCell(dataCellCenter("-"));
            }
        }

        document.add(table);
    }
}

