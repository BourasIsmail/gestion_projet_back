package ma.entraide.gestionprojet.rapport.service;

import com.lowagie.text.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ma.entraide.gestionprojet.entity.User;
import ma.entraide.gestionprojet.exception.ResourceNotFoundException;
import ma.entraide.gestionprojet.rapport.dto.RapportUserData;
import ma.entraide.gestionprojet.repository.EquipeRepository;
import ma.entraide.gestionprojet.repository.TacheRepository;
import ma.entraide.gestionprojet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

import static ma.entraide.gestionprojet.rapport.util.PdfGeneratorUtil.*;

@Service
@Transactional(readOnly = true)
public class RapportUserService {

    private final UserRepository userRepository;
    private final TacheRepository tacheRepository;
    private final EquipeRepository equipeRepository;

    public RapportUserService(UserRepository userRepository,
                              TacheRepository tacheRepository,
                              EquipeRepository equipeRepository) {
        this.userRepository = userRepository;
        this.tacheRepository = tacheRepository;
        this.equipeRepository = equipeRepository;
    }

    public byte[] genererRapportUser(Long userId, String generePar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve : " + userId));
        RapportUserData data = buildData(user);
        return genererPdf(data, generePar);
    }

    private RapportUserData buildData(User user) {
        var equipes = equipeRepository.findByMembreUserId(user.getId()).stream()
                .map(e -> e.getNom())
                .toList();

        var tachesEntites = tacheRepository.findByAssigneeUserId(user.getId());

        var taches = tachesEntites.stream()
                .map(t -> new RapportUserData.TacheUser(
                        t.getProjet().getNom(),
                        t.getTitre(),
                        t.getPriorite().name(),
                        t.getStatut().name(),
                        t.getDateEcheance(),
                        t.getPourcentage(),
                        t.isEnRetard()
                ))
                .toList();

        long completees = taches.stream().filter(t -> "TERMINEE".equals(t.statut())).count();
        long enCours = taches.stream().filter(t -> "EN_COURS".equals(t.statut())).count();
        long enRetard = taches.stream().filter(RapportUserData.TacheUser::enRetard).count();
        double taux = !taches.isEmpty() ? (double) completees / taches.size() * 100 : 0;

        return new RapportUserData(
                user.getNomComplet(),
                user.getEmail(),
                user.getRoleGlobal().name(),
                equipes,
                taches,
                completees,
                enCours,
                enRetard,
                taux
        );
    }

    private byte[] genererPdf(RapportUserData data, String generePar) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 50);
            document.setFooter(createFooter());
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, "RAPPORT D'ACTIVITE UTILISATEUR", generePar);

            document.add(sectionTitle("INFORMATIONS"));
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.addCell(labelValueCell("Nom", data.nomComplet()));
            infoTable.addCell(labelValueCell("Email", data.email()));
            infoTable.addCell(labelValueCell("Role", data.roleGlobal()));
            infoTable.addCell(labelValueCell("Equipes", String.join(", ", data.equipes())));
            document.add(infoTable);

            document.add(sectionTitle("INDICATEURS"));
            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.addCell(kpiCell("Completees", String.valueOf(data.tachesCompletees()), SUCCESS));
            kpiTable.addCell(kpiCell("En cours", String.valueOf(data.tachesEnCours()), new java.awt.Color(59, 130, 246)));
            kpiTable.addCell(kpiCell("En retard", String.valueOf(data.tachesEnRetard()), DANGER));
            kpiTable.addCell(kpiCell("Taux completion", String.format("%.0f%%", data.tauxCompletion()), PRIMARY));
            document.add(kpiTable);

            document.add(sectionTitle("TACHES ASSIGNEES (" + data.taches().size() + ")"));
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 3, 1.2f, 1.2f, 1.2f, 1});

            table.addCell(headerCell("Projet"));
            table.addCell(headerCell("Tache"));
            table.addCell(headerCell("Priorite"));
            table.addCell(headerCell("Statut"));
            table.addCell(headerCell("Echeance"));
            table.addCell(headerCell("Avanc."));

            for (int i = 0; i < data.taches().size(); i++) {
                var t = data.taches().get(i);
                PdfPCell projetCell = dataCell(t.projet());
                if (t.enRetard()) projetCell.setBackgroundColor(new java.awt.Color(254, 226, 226));
                else applyAlternateRowColor(projetCell, i);
                table.addCell(projetCell);

                PdfPCell titreCell = dataCell(t.titre());
                if (t.enRetard()) titreCell.setBackgroundColor(new java.awt.Color(254, 226, 226));
                else applyAlternateRowColor(titreCell, i);
                table.addCell(titreCell);

                table.addCell(prioriteCell(t.priorite()));
                table.addCell(statutCell(t.statut()));
                table.addCell(dataCellCenter(formatDate(t.dateEcheance())));
                table.addCell(dataCellCenter(t.pourcentage() + "%"));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur generation rapport utilisateur PDF", e);
        }
    }
}

