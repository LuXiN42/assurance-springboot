package com.assurance.assurance_springboot.service;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.Sinistre;
import com.assurance.assurance_springboot.model.StatutContrat;
import com.assurance.assurance_springboot.model.StatutSinistre;
import com.assurance.assurance_springboot.repository.ContratRepository;
import com.assurance.assurance_springboot.repository.SinistreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class SinistreService {
    private final SinistreRepository repo;
    private final ContratRepository contrats;
    private static final Map<StatutSinistre, Set<StatutSinistre>> TRANSITIONS = new EnumMap<>(StatutSinistre.class);
    static {
        TRANSITIONS.put(StatutSinistre.DECLARE, EnumSet.of(StatutSinistre.EN_ANALYSE, StatutSinistre.REFUSE));
        TRANSITIONS.put(StatutSinistre.EN_ANALYSE, EnumSet.of(StatutSinistre.DECLARE, StatutSinistre.ACCEPTE, StatutSinistre.REFUSE));
        TRANSITIONS.put(StatutSinistre.ACCEPTE, EnumSet.of(StatutSinistre.EN_ANALYSE, StatutSinistre.INDEMNISE));
        TRANSITIONS.put(StatutSinistre.REFUSE, EnumSet.noneOf(StatutSinistre.class));
        TRANSITIONS.put(StatutSinistre.INDEMNISE, EnumSet.noneOf(StatutSinistre.class));
    }
    public SinistreService(SinistreRepository repo, ContratRepository contrats) { this.repo=repo; this.contrats=contrats; }
    @Transactional(readOnly=true) public List<Sinistre> lister() { return repo.findAll(); }
    @Transactional(readOnly=true) public Sinistre trouver(Long id) { return repo.findById(id).orElseThrow(()->new BusinessException("Sinistre introuvable : " + id)); }
    public Sinistre declarer(Long contratId, LocalDate date, String description, java.math.BigDecimal montant) {
        Contrat contrat=contrats.findById(contratId).orElseThrow(()->new BusinessException("Contrat introuvable."));
        if (contrat.getStatut()!=StatutContrat.ACTIF) throw new BusinessException("Un sinistre ne peut être déclaré que sur un contrat ACTIF.");
        if (date==null || date.isAfter(LocalDate.now())) throw new BusinessException("La date du sinistre est obligatoire et ne peut pas être future.");
        if (date.isBefore(contrat.getDateDebut()) || (contrat.getDateFin()!=null && date.isAfter(contrat.getDateFin()))) throw new BusinessException("La date du sinistre doit se situer dans la période du contrat.");
        if (description==null || description.trim().length()<10 || description.length()>2000) throw new BusinessException("La description doit contenir de 10 à 2000 caractères.");
        if (montant!=null && montant.signum()<0) throw new BusinessException("Le montant estimé ne peut pas être négatif.");
        Sinistre s=new Sinistre(); s.setNumeroSinistre("SIN-"+System.currentTimeMillis()); s.setDate(date); s.setDescription(description.trim()); s.setMontantEstime(montant); s.setStatut(StatutSinistre.DECLARE); s.setContrat(contrat);
        return repo.save(s);
    }
    public Sinistre changerStatut(Long id, StatutSinistre cible, java.math.BigDecimal montantIndemnisation) {
        if (cible==null) throw new BusinessException("Statut invalide.");
        Sinistre s=trouver(id);
        if (s.getStatut()!=cible && !TRANSITIONS.getOrDefault(s.getStatut(), Set.of()).contains(cible)) throw new BusinessException("Transition invalide de " + s.getStatut() + " à " + cible + ".");
        if (cible==StatutSinistre.INDEMNISE) {
            if (s.getStatut()!=StatutSinistre.ACCEPTE) throw new BusinessException("Seul un sinistre accepté peut être indemnisé.");
            if (montantIndemnisation==null || montantIndemnisation.signum()<=0) throw new BusinessException("Le montant d'indemnisation doit être strictement positif.");
            s.setMontantIndemnisation(montantIndemnisation);
        } else if (cible!=StatutSinistre.ACCEPTE) {
            s.setMontantIndemnisation(null);
        }
        s.setStatut(cible); return repo.save(s);
    }
    public void supprimer(Long id) { repo.delete(trouver(id)); }
}
