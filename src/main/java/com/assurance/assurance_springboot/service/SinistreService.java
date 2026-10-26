package com.assurance.assurance_springboot.service;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.PieceJustificative;
import com.assurance.assurance_springboot.model.Sinistre;
import com.assurance.assurance_springboot.model.StatutContrat;
import com.assurance.assurance_springboot.model.StatutSinistre;
import com.assurance.assurance_springboot.model.Utilisateur;
import com.assurance.assurance_springboot.repository.ContratRepository;
import com.assurance.assurance_springboot.repository.SinistreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class SinistreService {
    private final SinistreRepository sinistreRepository;
    private final ContratRepository contratRepository;
    private static final Map<StatutSinistre, Set<StatutSinistre>> TRANSITIONS;
    static {
        TRANSITIONS = new EnumMap<>(StatutSinistre.class);
        TRANSITIONS.put(StatutSinistre.DECLARE, EnumSet.of(StatutSinistre.EN_ANALYSE, StatutSinistre.REFUSE));
        TRANSITIONS.put(StatutSinistre.EN_ANALYSE, EnumSet.of(StatutSinistre.DECLARE, StatutSinistre.ACCEPTE, StatutSinistre.REFUSE));
        TRANSITIONS.put(StatutSinistre.ACCEPTE, EnumSet.of(StatutSinistre.EN_ANALYSE, StatutSinistre.INDEMNISE));
        TRANSITIONS.put(StatutSinistre.REFUSE, EnumSet.noneOf(StatutSinistre.class));
        TRANSITIONS.put(StatutSinistre.INDEMNISE, EnumSet.noneOf(StatutSinistre.class));
    }

    public SinistreService(SinistreRepository sinistreRepository, ContratRepository contratRepository) {
        this.sinistreRepository = sinistreRepository; this.contratRepository = contratRepository;
    }

    @Transactional
    public Sinistre declarer(Long clientId, Long contratId, LocalDate date, String description,
                             String typeSinistre, BigDecimal montantEstime, List<PieceJustificative> pieces) {
        if (clientId == null) throw new BusinessException("Client non identifié.");
        if (contratId == null) throw new BusinessException("Veuillez choisir un contrat.");
        if (date == null) throw new BusinessException("La date du sinistre est obligatoire.");
        if (date.isAfter(LocalDate.now())) throw new BusinessException("La date du sinistre ne peut pas être dans le futur.");
        Contrat contrat = contratRepository.findByIdEtClient(contratId, clientId).orElse(null);
        if (contrat == null) throw new BusinessException("Contrat invalide ou n'appartenant pas au client.");
        if (contrat.getStatut() != StatutContrat.ACTIF)
            throw new BusinessException("Le contrat n'est pas actif (statut = " + contrat.getStatut() + ").");
        if (date.isBefore(contrat.getDateDebut())) throw new BusinessException("La date du sinistre est antérieure au début du contrat.");
        if (contrat.getDateFin() != null && date.isAfter(contrat.getDateFin()))
            throw new BusinessException("La date du sinistre est postérieure à la fin du contrat.");
        if (description == null || description.length() < 10 || description.length() > 2000)
            throw new BusinessException("La description doit contenir entre 10 et 2000 caractères.");
        if (typeSinistre == null || typeSinistre.isBlank() || typeSinistre.length() > 120)
            throw new BusinessException("Le type de sinistre est obligatoire (120 caractères max).");
        if (montantEstime != null && montantEstime.signum() < 0) throw new BusinessException("Le montant estimé ne peut pas être négatif.");

        Sinistre s = new Sinistre();
        s.setNumeroSinistre(String.format("SIN-%d-%05d", LocalDate.now().getYear(), sinistreRepository.count() + 1));
        s.setDate(date); s.setDescription(description); s.setTypeSinistre(typeSinistre);
        s.setMontantEstime(montantEstime); s.setStatut(StatutSinistre.DECLARE); s.setContrat(contrat);
        if (pieces != null) for (PieceJustificative p : pieces) s.ajouterPiece(p);
        return sinistreRepository.save(s);
    }

    @Transactional
    public void changerStatut(Long sinistreId, StatutSinistre nouveauStatut) {
        if (sinistreId == null) throw new BusinessException("Identifiant de sinistre manquant.");
        if (nouveauStatut == null) throw new BusinessException("Statut invalide.");
        if (nouveauStatut == StatutSinistre.INDEMNISE)
            throw new BusinessException("Utilisez l'action 'Indemniser' pour passer à INDEMNISE (montant obligatoire).");
        Sinistre s = loadSinistre(sinistreId); verifierTransition(s.getStatut(), nouveauStatut);
        s.setStatut(nouveauStatut); sinistreRepository.save(s);
    }

    @Transactional
    public void indemniser(Long sinistreId, BigDecimal montant) {
        if (sinistreId == null) throw new BusinessException("Identifiant de sinistre manquant.");
        if (montant == null || montant.signum() <= 0)
            throw new BusinessException("Le montant d'indemnisation doit être strictement positif.");
        Sinistre s = loadSinistre(sinistreId);
        if (s.getStatut() != StatutSinistre.ACCEPTE)
            throw new BusinessException("Indemnisation impossible : le sinistre doit être ACCEPTE (statut courant : " + s.getStatut() + ").");
        s.setMontantIndemnisation(montant); s.setStatut(StatutSinistre.INDEMNISE); sinistreRepository.save(s);
    }

    public Sinistre loadSinistre(Long sinistreId) {
        if (sinistreId == null) throw new BusinessException("Sinistre introuvable : " + sinistreId);
        return sinistreRepository.findDetailById(sinistreId).orElseThrow(() -> new BusinessException("Sinistre introuvable : " + sinistreId));
    }

    private void verifierTransition(StatutSinistre courant, StatutSinistre cible) {
        if (courant == cible) return;
        Set<StatutSinistre> autorisees = TRANSITIONS.getOrDefault(courant, EnumSet.noneOf(StatutSinistre.class));
        if (!autorisees.contains(cible)) throw new BusinessException("Transition de statut invalide : " + courant + " → " + cible
                + ". Transitions autorisées depuis " + courant + " : " + autorisees);
    }

    public static Map<StatutSinistre, Set<StatutSinistre>> transitionsAutorisees() {
        Map<StatutSinistre, Set<StatutSinistre>> copie = new EnumMap<>(StatutSinistre.class);
        TRANSITIONS.forEach((k, v) -> copie.put(k, v.isEmpty() ? EnumSet.noneOf(StatutSinistre.class) : EnumSet.copyOf(v)));
        return copie;
    }
    public Utilisateur clientDuSinistre(Sinistre s) { return s == null ? null : s.getClient(); }
}
