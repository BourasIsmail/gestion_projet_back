package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.dto.TypeProjetDTO;
import ma.entraide.gestionprojet.entity.TacheModele;
import ma.entraide.gestionprojet.entity.TypeProjet;
import ma.entraide.gestionprojet.entity.enums.Priorite;
import ma.entraide.gestionprojet.exception.BadRequestException;
import ma.entraide.gestionprojet.exception.ResourceNotFoundException;
import ma.entraide.gestionprojet.repository.TacheModeleRepository;
import ma.entraide.gestionprojet.repository.TypeProjetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TypeProjetService {

    private final TypeProjetRepository typeProjetRepository;
    private final TacheModeleRepository tacheModeleRepository;

    public TypeProjetService(TypeProjetRepository typeProjetRepository,
                             TacheModeleRepository tacheModeleRepository) {
        this.typeProjetRepository = typeProjetRepository;
        this.tacheModeleRepository = tacheModeleRepository;
    }

    public List<TypeProjetDTO> getAllTypes() {
        return typeProjetRepository.findByActifTrue().stream()
                .map(TypeProjetDTO::fromEntity)
                .toList();
    }

    public TypeProjetDTO getTypeById(Long id) {
        return TypeProjetDTO.fromEntity(findTypeOrThrow(id));
    }

    public TypeProjetDTO createType(String code, String libelle, String description) {
        if (typeProjetRepository.existsByCode(code)) {
            throw new BadRequestException("Un type avec ce code existe deja");
        }
        TypeProjet type = new TypeProjet();
        type.setCode(code.toUpperCase());
        type.setLibelle(libelle);
        type.setDescription(description);
        type.setActif(true);
        return TypeProjetDTO.fromEntity(typeProjetRepository.save(type));
    }

    public TypeProjetDTO updateType(Long id, String libelle, String description) {
        TypeProjet type = findTypeOrThrow(id);
        if (libelle != null) type.setLibelle(libelle);
        if (description != null) type.setDescription(description);
        return TypeProjetDTO.fromEntity(typeProjetRepository.save(type));
    }

    public List<TypeProjetDTO.TacheModeleDTO> getTachesModeles(Long typeId) {
        findTypeOrThrow(typeId);
        return tacheModeleRepository.findByTypeProjetIdOrderByOrdreAsc(typeId).stream()
                .map(tm -> new TypeProjetDTO.TacheModeleDTO(
                        tm.getId(), tm.getTitre(), tm.getDescription(),
                        tm.getPriorite().name(), tm.getOrdre(), tm.getDelaiJours()))
                .toList();
    }

    public TypeProjetDTO.TacheModeleDTO addTacheModele(Long typeId, String titre, String description,
                                                       String priorite, Integer ordre, Integer delaiJours) {
        TypeProjet type = findTypeOrThrow(typeId);
        TacheModele modele = new TacheModele();
        modele.setTypeProjet(type);
        modele.setTitre(titre);
        modele.setDescription(description);
        modele.setPriorite(Priorite.valueOf(priorite));
        modele.setOrdre(ordre != null ? ordre : 0);
        modele.setDelaiJours(delaiJours);
        modele = tacheModeleRepository.save(modele);
        return new TypeProjetDTO.TacheModeleDTO(
                modele.getId(), modele.getTitre(), modele.getDescription(),
                modele.getPriorite().name(), modele.getOrdre(), modele.getDelaiJours());
    }

    public TypeProjetDTO.TacheModeleDTO updateTacheModele(Long id, String titre, String description,
                                                          String priorite, Integer ordre, Integer delaiJours) {
        TacheModele modele = tacheModeleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modele de tache non trouve"));
        if (titre != null) modele.setTitre(titre);
        if (description != null) modele.setDescription(description);
        if (priorite != null) modele.setPriorite(Priorite.valueOf(priorite));
        if (ordre != null) modele.setOrdre(ordre);
        if (delaiJours != null) modele.setDelaiJours(delaiJours);
        modele = tacheModeleRepository.save(modele);
        return new TypeProjetDTO.TacheModeleDTO(
                modele.getId(), modele.getTitre(), modele.getDescription(),
                modele.getPriorite().name(), modele.getOrdre(), modele.getDelaiJours());
    }

    public void deleteTacheModele(Long id) {
        if (!tacheModeleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Modele de tache non trouve");
        }
        tacheModeleRepository.deleteById(id);
    }

    private TypeProjet findTypeOrThrow(Long id) {
        return typeProjetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type de projet non trouve avec l'id : " + id));
    }
}

