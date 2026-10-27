package com.assurance.assurance_springboot.service;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.ModePaiement;
import com.assurance.assurance_springboot.model.Paiement;
import com.assurance.assurance_springboot.model.StatutContrat;
import com.assurance.assurance_springboot.model.StatutPaiement;
import com.assurance.assurance_springboot.repository.ContratRepository;
import com.assurance.assurance_springboot.repository.PaiementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final ContratRepository contratRepository;

    public PaiementService(
            PaiementRepository paiementRepository,
            ContratRepository contratRepository
    ) {
        this.paiementRepository = paiementRepository;
        this.contratRepository = contratRepository;
    }

    public List<Paiement> tous() {
        return paiementRepository.findAll();
    }

    public List<Paiement> enAttente() {
        return paiementRepository.findByStatut(StatutPaiement.EN_ATTENTE);
    }

    public List<Paiement> echeances(int jours) {
        if (jours < 0) {
            throw new BusinessException(
                    "Le nombre de jours ne peut pas être négatif."
            );
        }

        return paiementRepository.findEcheancesGlobales(
                StatutPaiement.EN_ATTENTE,
                LocalDate.now(),
                LocalDate.now().plusDays(jours)
        );
    }

    public List<Paiement> parStatut(StatutPaiement s) {
        return paiementRepository.findByStatut(s);
    }

    public List<Paiement> parContrat(Long contratId) {
        return paiementRepository.findByContratIdOrderByDateDesc(contratId);
    }

    public List<Paiement> historiqueClient(Long cid) {
        return paiementRepository.findByContratClientIdOrderByDateDesc(cid);
    }

    public List<Paiement> effectuesClient(Long cid) {
        return paiementRepository.findByContratClientIdAndStatutOrderByDateDesc(
                cid,
                StatutPaiement.EFFECTUE
        );
    }

    public List<Paiement> enAttenteClient(Long cid) {
        return paiementRepository.findByContratClientIdAndStatutOrderByDateDesc(
                cid,
                StatutPaiement.EN_ATTENTE
        );
    }

    public List<Paiement> echeancesClient(Long cid, int jours) {
        return paiementRepository.findEcheances(
                cid,
                StatutPaiement.EN_ATTENTE,
                LocalDate.now(),
                LocalDate.now().plusDays(jours)
        );
    }

    public Paiement detail(Long id) {
        if (id == null) {
            throw new BusinessException("Paiement introuvable : " + id);
        }

        return paiementRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("Paiement introuvable : " + id)
                );
    }

    @Transactional
    public Paiement enregistrer(
            Long contratId,
            BigDecimal montant,
            LocalDate date,
            ModePaiement mode,
            StatutPaiement statut
    ) {
        if (contratId == null) {
            throw new BusinessException("Le contrat est obligatoire.");
        }

        if (montant == null || montant.signum() <= 0) {
            throw new BusinessException(
                    "Le montant doit être strictement positif."
            );
        }

        if (statut == null) {
            statut = StatutPaiement.EFFECTUE;
        }

        if (statut != StatutPaiement.EFFECTUE
                && statut != StatutPaiement.EN_ATTENTE) {
            throw new BusinessException(
                    "Un nouveau paiement est soit EFFECTUE, soit EN_ATTENTE."
            );
        }

        if (statut == StatutPaiement.EFFECTUE && mode == null) {
            throw new BusinessException(
                    "Le mode de paiement est obligatoire pour un paiement effectué."
            );
        }

        if (date == null) {
            date = LocalDate.now();
        }

        if (statut == StatutPaiement.EFFECTUE && date.isAfter(LocalDate.now())) {
            throw new BusinessException(
                    "Un paiement effectué ne peut pas avoir une date future."
            );
        }

        Contrat c = contratRepository.findById(contratId).orElse(null);

        if (c == null) {
            throw new BusinessException("Contrat introuvable : " + contratId);
        }

        if (c.getStatut() == StatutContrat.RESILIE) {
            throw new BusinessException(
                    "Impossible d'enregistrer un paiement sur un contrat résilié."
            );
        }

        Paiement p = new Paiement();
        p.setContrat(c);
        p.setMontant(montant);
        p.setDate(date);
        p.setModePaiement(mode);
        p.setStatut(statut);

        p = paiementRepository.save(p);

        if (statut == StatutPaiement.EFFECTUE) {
            activerSiPremierPaiement(c);
        }

        return p;
    }

    @Transactional
    public Paiement valider(Long paiementId, ModePaiement mode) {
        Paiement p = detail(paiementId);

        if (p.getStatut() != StatutPaiement.EN_ATTENTE) {
            throw new BusinessException(
                    "Seul un paiement EN_ATTENTE peut être validé (statut : "
                            + p.getStatut() + ")."
            );
        }

        ModePaiement m = mode != null ? mode : p.getModePaiement();

        if (m == null) {
            throw new BusinessException(
                    "Le mode de paiement est obligatoire pour valider."
            );
        }

        p.setStatut(StatutPaiement.EFFECTUE);
        p.setModePaiement(m);
        paiementRepository.save(p);

        activerSiPremierPaiement(p.getContrat());

        return detail(paiementId);
    }

    @Transactional
    public Paiement echouer(Long paiementId) {
        return transition(paiementId, StatutPaiement.ECHOUE);
    }

    @Transactional
    public Paiement annuler(Long paiementId) {
        return transition(paiementId, StatutPaiement.ANNULE);
    }

    private Paiement transition(Long paiementId, StatutPaiement cible) {
        Paiement p = detail(paiementId);

        if (p.getStatut() != StatutPaiement.EN_ATTENTE) {
            throw new BusinessException(
                    "Seul un paiement EN_ATTENTE peut passer en "
                            + cible + " (statut : " + p.getStatut() + ")."
            );
        }

        p.setStatut(cible);
        paiementRepository.save(p);

        return detail(paiementId);
    }

    private void activerSiPremierPaiement(Contrat c) {
        if (c.getStatut() == StatutContrat.EN_ATTENTE) {
            c.setStatut(StatutContrat.ACTIF);
            contratRepository.save(c);
        }
    }
}