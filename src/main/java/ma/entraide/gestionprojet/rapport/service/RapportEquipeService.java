package ma.entraide.gestionprojet.rapport.service;

import com.lowagie.text.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ma.entraide.gestionprojet.entity.Equipe;
import ma.entraide.gestionprojet.entity.Tache;
import ma.entraide.gestionprojet.entity.enums.RoleEquipe;
import ma.entraide.gestionprojet.entity.enums.StatutProjet;
import ma.entraide.gestionprojet.entity.enums.StatutTache;
import ma.entraide.gestionprojet.exception.ResourceNotFoundException;
import ma.entraide.gestionprojet.rapport.dto.RapportEquipeData;
import ma.entraide.gestionprojet.repository.EquipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static ma.entraide.gestionprojet.rapport.util.PdfGeneratorUtil.*;

@Service
@Transactional(readOnly = true)
public class RapportEquipeService {

    private final EquipeRepository equipeRepository;

    public RapportEquipeService(EquipeRepository equipeRepository) {
        this.equipeRepository = equipeRepository;
    }

    public byte[] genererRapportEquipe(Long equipeId, String generePar) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe non trouvee : " + equipeId));
        RapportEquipeData data = buildData(equipe);
        return genererPdf(data, generePar);
    }

    public byte[] genererRapportProjetsEquipe(Long equipeId, String generePar) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe non trouvee : " + equipeId));
        RapportEquipeData data = buildData(equipe);
        return genererPdfProjets(data, generePar);
    }

    private RapportEquipeData buildData(Equipe equipe) {
        String chefEquipe = equipe.getMembres().stream()
                .filter(m -> m.getRoleEquipe() == RoleEquipe.CHEF_EQUIPE)
                .map(m -> m.getUser().getNomComplet())
                .findFirst()
                .orElse("Non defini");

        var membres = equipe.getMembres().stream()
                .map(m -> new RapportEquipeData.MembreInfo(m.getUser().getNomComplet(), m.getRoleEquipe().name()))
                .toList();

        var projets = equipe.getProjets().stream()
                .map(p -> {
                    long tachesEnRetard = p.getTaches().stream().filter(Tache::isEnRetard).count();
                    return new RapportEquipeData.ProjetResume(
                            p.getNom(),
                            p.getTypeProjet().getLibelle(),
                            p.getStatut().name(),
                            p.getPriorite().name(),
                            p.getPourcentageProgression(),
                            p.getDateFinPrevue(),
                            tachesEnRetard
                    );
                })
                .toList();

        long projetsActifs = equipe.getProjets().stream()
                .filter(p -> p.getStatut() != StatutProjet.TERMINE && p.getStatut() != StatutProjet.ANNULE)
                .count();
        long projetsTermines = equipe.getProjets().stream()
                .filter(p -> p.getStatut() == StatutProjet.TERMINE)
                .count();

        var allTaches = equipe.getProjets().stream()
                .flatMap(p -> p.getTaches().stream())
                .toList();

        long totalTaches = allTaches.size();
        long tachesTerminees = allTaches.stream().filter(t -> t.getStatut() == StatutTache.TERMINEE).count();
        long tachesEnRetard = allTaches.stream().filter(Tache::isEnRetard).count();

        double tauxCompletion = totalTaches > 0 ? (double) tachesTerminees / totalTaches * 100 : 0;
        double delaiMoyen = allTaches.stream()
                .filter(Tache::isEnRetard)
                .mapToLong(Tache::getJoursRetard)
                .average()
                .orElse(0);

        return new RapportEquipeData(
                equipe.getNom(),
                equipe.getDescription(),
                chefEquipe,
                membres.size(),
                membres,
                projets,
                projetsActifs,
                projetsTermines,
                totalTaches,
                tachesTerminees,
                tachesEnRetard,
                tauxCompletion,
                delaiMoyen
        );
    }

    private byte[] genererPdf(RapportEquipeData data, String generePar) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 50);
            document.setFooter(createFooter());
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, "RAPPORT D'EQUIPE", generePar);

            document.add(sectionTitle("INFORMATIONS DE L'EQUIPE"));
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.addCell(labelValueCell("Equipe", data.nomEquipe()));
            infoTable.addCell(labelValueCell("Chef d'equipe", data.chefEquipe()));
            infoTable.addCell(labelValueCell("Membres", String.valueOf(data.nombreMembres())));
            infoTable.addCell(labelValueCell("Description", data.description() != null ? data.description() : "-"));
            document.add(infoTable);

            document.add(sectionTitle("INDICATEURS CLES"));
            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.addCell(kpiCell("Projets actifs", String.valueOf(data.projetsActifs()), PRIMARY));
            kpiTable.addCell(kpiCell("Taux completion", String.format("%.0f%%", data.tauxCompletion()), SUCCESS));
            kpiTable.addCell(kpiCell("Taches en retard", String.valueOf(data.tachesEnRetard()), DANGER));
            kpiTable.addCell(kpiCell("Retard moyen", String.format("%.1fj", data.delaiMoyenRetard()), WARNING));
            document.add(kpiTable);

            document.add(sectionTitle("MEMBRES"));
            PdfPTable membreTable = new PdfPTable(2);
            membreTable.setWidthPercentage(60);
            membreTable.addCell(headerCell("Nom"));
            membreTable.addCell(headerCell("Role"));
            for (int i = 0; i < data.membres().size(); i++) {
                var m = data.membres().get(i);
                PdfPCell nomCell = dataCell(m.nom());
                PdfPCell roleCell = dataCellCenter(m.role());
                applyAlternateRowColor(nomCell, i);
                applyAlternateRowColor(roleCell, i);
                membreTable.addCell(nomCell);
                membreTable.addCell(roleCell);
            }
            document.add(membreTable);

            addProjetsTable(document, data);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur generation rapport equipe PDF", e);
        }
    }

    private byte[] genererPdfProjets(RapportEquipeData data, String generePar) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 50);
            document.setFooter(createFooter());
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, "SYNTHESE PROJETS - " + data.nomEquipe(), generePar);

            addProjetsTable(document, data);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur generation rapport projets equipe PDF", e);
        }
    }

    private void addProjetsTable(Document document, RapportEquipeData data) throws DocumentException {
        document.add(sectionTitle("PROJETS DE L'EQUIPE"));
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1.5f, 1.2f, 1.2f, 1, 1.2f, 1});

        table.addCell(headerCell("Projet"));
        table.addCell(headerCell("Type"));
        table.addCell(headerCell("Statut"));
        table.addCell(headerCell("Priorite"));
        table.addCell(headerCell("Progr."));
        table.addCell(headerCell("Deadline"));
        table.addCell(headerCell("Retards"));

        for (int i = 0; i < data.projets().size(); i++) {
            var p = data.projets().get(i);
            PdfPCell nomCell = dataCell(p.nom());
            applyAlternateRowColor(nomCell, i);
            table.addCell(nomCell);

            PdfPCell typeCell = dataCellCenter(p.type());
            applyAlternateRowColor(typeCell, i);
            table.addCell(typeCell);

            table.addCell(statutCell(p.statut()));
            table.addCell(prioriteCell(p.priorite()));
            table.addCell(dataCellCenter(p.progression() + "%"));
            table.addCell(dataCellCenter(formatDate(p.deadline())));

            if (p.tachesEnRetard() > 0) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(p.tachesEnRetard()), dangerFont())));
            } else {
                table.addCell(dataCellCenter("0"));
            }
        }
        document.add(table);
    }
}

