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
import ma.entraide.gestionprojet.entity.Projet;
import ma.entraide.gestionprojet.entity.enums.StatutProjet;
import ma.entraide.gestionprojet.entity.enums.StatutTache;
import ma.entraide.gestionprojet.rapport.dto.RapportGlobalData;
import ma.entraide.gestionprojet.repository.EquipeRepository;
import ma.entraide.gestionprojet.repository.ProjetRepository;
import ma.entraide.gestionprojet.repository.TacheRepository;
import ma.entraide.gestionprojet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ma.entraide.gestionprojet.rapport.util.PdfGeneratorUtil.*;

@Service
@Transactional(readOnly = true)
public class RapportGlobalService {

    private final UserRepository userRepository;
    private final EquipeRepository equipeRepository;
    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;

    public RapportGlobalService(UserRepository userRepository,
                                EquipeRepository equipeRepository,
                                ProjetRepository projetRepository,
                                TacheRepository tacheRepository) {
        this.userRepository = userRepository;
        this.equipeRepository = equipeRepository;
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
    }

    public byte[] genererRapportGlobal(String generePar) {
        RapportGlobalData data = buildData();
        return genererPdf(data, generePar);
    }

    private RapportGlobalData buildData() {
        long usersActifs = userRepository.countByActifTrue();
        long nbEquipes = equipeRepository.countByActifTrue();
        long nbProjets = projetRepository.count();
        long nbTaches = tacheRepository.count();

        Map<String, Long> projetsParType = projetRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getTypeProjet().getLibelle(),
                        Collectors.counting()
                ));

        Map<String, Long> projetsParStatut = new LinkedHashMap<>();
        for (StatutProjet s : StatutProjet.values()) {
            long count = projetRepository.countByStatut(s);
            if (count > 0) projetsParStatut.put(s.name(), count);
        }

        List<RapportGlobalData.ProjetRetard> top5Retard = projetRepository.findAll().stream()
                .filter(p -> p.getStatut() != StatutProjet.TERMINE && p.getStatut() != StatutProjet.ANNULE)
                .filter(p -> p.getDateFinPrevue() != null && p.getDateFinPrevue().isBefore(LocalDate.now()))
                .sorted(Comparator.comparingLong(p -> -java.time.temporal.ChronoUnit.DAYS.between(
                        ((Projet) p).getDateFinPrevue(), LocalDate.now())).thenComparing(p -> ((Projet) p).getNom()))
                .limit(5)
                .map(p -> new RapportGlobalData.ProjetRetard(
                        p.getNom(),
                        p.getEquipe().getNom(),
                        java.time.temporal.ChronoUnit.DAYS.between(p.getDateFinPrevue(), LocalDate.now())
                ))
                .toList();

        List<RapportGlobalData.EquipePerf> top5Equipes = equipeRepository.findByActifTrue().stream()
                .map(e -> {
                    var allTaches = e.getProjets().stream()
                            .flatMap(p -> p.getTaches().stream())
                            .toList();
                    long terminees = allTaches.stream()
                            .filter(t -> t.getStatut() == StatutTache.TERMINEE).count();
                    double taux = !allTaches.isEmpty() ? (double) terminees / allTaches.size() * 100 : 0;
                    long projetsTermines = e.getProjets().stream()
                            .filter(p -> p.getStatut() == StatutProjet.TERMINE).count();
                    return new RapportGlobalData.EquipePerf(e.getNom(), taux, projetsTermines);
                })
                .sorted(Comparator.comparingDouble(RapportGlobalData.EquipePerf::tauxCompletion).reversed())
                .limit(5)
                .toList();

        long alertesActives = tacheRepository.countTachesEnRetard();

        return new RapportGlobalData(
                usersActifs,
                nbEquipes,
                nbProjets,
                nbTaches,
                projetsParType,
                projetsParStatut,
                top5Retard,
                top5Equipes,
                alertesActives,
                alertesActives
        );
    }

    private byte[] genererPdf(RapportGlobalData data, String generePar) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 50);
            document.setFooter(createFooter());
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, "RAPPORT GLOBAL DE SYNTHESE", generePar);

            document.add(sectionTitle("INDICATEURS GENERAUX"));
            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.addCell(kpiCell("Utilisateurs", String.valueOf(data.utilisateursActifs()), PRIMARY));
            kpiTable.addCell(kpiCell("Equipes", String.valueOf(data.nombreEquipes()), PRIMARY));
            kpiTable.addCell(kpiCell("Projets", String.valueOf(data.nombreProjets()), PRIMARY));
            kpiTable.addCell(kpiCell("Taches", String.valueOf(data.nombreTaches()), PRIMARY));
            document.add(kpiTable);

            PdfPTable alerteKpi = new PdfPTable(2);
            alerteKpi.setWidthPercentage(50);
            alerteKpi.setSpacingBefore(5);
            alerteKpi.addCell(kpiCell("Alertes actives", String.valueOf(data.alertesActives()), DANGER));
            alerteKpi.addCell(kpiCell("Taches en retard", String.valueOf(data.tachesEnRetardTotal()), WARNING));
            document.add(alerteKpi);

            document.add(sectionTitle("REPARTITION DES PROJETS PAR TYPE"));
            PdfPTable typeTable = new PdfPTable(2);
            typeTable.setWidthPercentage(60);
            typeTable.addCell(headerCell("Type"));
            typeTable.addCell(headerCell("Nombre"));
            int i = 0;
            for (var entry : data.projetsParType().entrySet()) {
                PdfPCell labelCell = dataCell(entry.getKey());
                PdfPCell valCell = dataCellCenter(String.valueOf(entry.getValue()));
                applyAlternateRowColor(labelCell, i);
                applyAlternateRowColor(valCell, i);
                typeTable.addCell(labelCell);
                typeTable.addCell(valCell);
                i++;
            }
            document.add(typeTable);

            document.add(sectionTitle("REPARTITION DES PROJETS PAR STATUT"));
            PdfPTable statutTable = new PdfPTable(2);
            statutTable.setWidthPercentage(60);
            statutTable.addCell(headerCell("Statut"));
            statutTable.addCell(headerCell("Nombre"));
            i = 0;
            for (var entry : data.projetsParStatut().entrySet()) {
                statutTable.addCell(statutCell(entry.getKey()));
                PdfPCell valCell = dataCellCenter(String.valueOf(entry.getValue()));
                applyAlternateRowColor(valCell, i);
                statutTable.addCell(valCell);
                i++;
            }
            document.add(statutTable);

            if (!data.top5ProjetsEnRetard().isEmpty()) {
                document.add(sectionTitle("TOP 5 PROJETS LES PLUS EN RETARD"));
                PdfPTable retardTable = new PdfPTable(3);
                retardTable.setWidthPercentage(80);
                retardTable.addCell(headerCell("Projet"));
                retardTable.addCell(headerCell("Equipe"));
                retardTable.addCell(headerCell("Jours retard"));

                for (int j = 0; j < data.top5ProjetsEnRetard().size(); j++) {
                    var pr = data.top5ProjetsEnRetard().get(j);
                    PdfPCell nomCell = dataCell(pr.nom());
                    applyAlternateRowColor(nomCell, j);
                    retardTable.addCell(nomCell);

                    PdfPCell equipeCell = dataCell(pr.equipe());
                    applyAlternateRowColor(equipeCell, j);
                    retardTable.addCell(equipeCell);

                    retardTable.addCell(new PdfPCell(new Phrase(pr.joursRetard() + "j", dangerFont())));
                }
                document.add(retardTable);
            }

            if (!data.top5EquipesPerformantes().isEmpty()) {
                document.add(sectionTitle("TOP 5 EQUIPES LES PLUS PERFORMANTES"));
                PdfPTable perfTable = new PdfPTable(3);
                perfTable.setWidthPercentage(80);
                perfTable.addCell(headerCell("Equipe"));
                perfTable.addCell(headerCell("Taux completion"));
                perfTable.addCell(headerCell("Projets termines"));

                for (int j = 0; j < data.top5EquipesPerformantes().size(); j++) {
                    var ep = data.top5EquipesPerformantes().get(j);
                    PdfPCell nomCell = dataCell(ep.nom());
                    applyAlternateRowColor(nomCell, j);
                    perfTable.addCell(nomCell);

                    PdfPCell tauxCell = dataCellCenter(String.format("%.0f%%", ep.tauxCompletion()));
                    applyAlternateRowColor(tauxCell, j);
                    perfTable.addCell(tauxCell);

                    PdfPCell projCell = dataCellCenter(String.valueOf(ep.projetsTermines()));
                    applyAlternateRowColor(projCell, j);
                    perfTable.addCell(projCell);
                }
                document.add(perfTable);
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur generation rapport global PDF", e);
        }
    }
}
