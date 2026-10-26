package com.assurance.assurance_springboot.service;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.Garantie;
import com.assurance.assurance_springboot.model.StatutContrat;
import com.assurance.assurance_springboot.model.TypeAssurance;
import com.assurance.assurance_springboot.repository.ContratRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Service métier des contrats. */
@Service
@Transactional(readOnly = true)
public class ContratService {
    private final ContratRepository contratRepository;

    public ContratService(ContratRepository contratRepository) {
        this.contratRepository = contratRepository;
    }

    public List<Contrat> rechercher(String numero, String nomClient, Long typeId, StatutContrat statut) {
        String n = numero == null || numero.isBlank() ? null : numero.trim();
        String nom = nomClient == null || nomClient.isBlank() ? null : nomClient.toLowerCase().trim();
        return contratRepository.rechercher(n, nom, typeId, statut);
    }

    public List<Contrat> contratsDuClient(Long clientId) {
        return contratRepository.findByClientIdOrderByDateDebutDesc(clientId);
    }

    public Contrat detail(Long id) { return mustExist(id); }

    public Contrat detailPourClient(Long contratId, Long clientId) {
        Contrat c = mustExist(contratId);
        if (!c.getClient().getId().equals(clientId)) throw new BusinessException("Ce contrat ne vous appartient pas.");
        return c;
    }

    public boolean estActif(Contrat c) {
        if (c == null || c.getStatut() != StatutContrat.ACTIF) return false;
        LocalDate today = LocalDate.now();
        if (c.getDateDebut() != null && today.isBefore(c.getDateDebut())) return false;
        return c.getDateFin() == null || !today.isAfter(c.getDateFin());
    }

    public Contrat verifierContratActif(Long contratId) {
        Contrat c = mustExist(contratId);
        if (!estActif(c)) throw new BusinessException("Le contrat " + c.getNumeroContrat()
                + " n'est pas actif (statut = " + c.getStatut() + ").");
        return c;
    }

    public Contrat verifierContratDuClient(Long contratId, Long clientId) {
        return contratRepository.findByIdEtClient(contratId, clientId)
                .orElseThrow(() -> new BusinessException("Contrat invalide ou n'appartenant pas au client."));
    }

    @Transactional
    public Contrat souscrire(Contrat c) {
        if (c == null) throw new BusinessException("Contrat nul.");
        if (c.getNumeroContrat() == null || c.getNumeroContrat().isBlank()) {
            c.setNumeroContrat(String.format("CTR-%d-%05d", LocalDate.now().getYear(), contratRepository.count() + 1));
        } else if (contratRepository.existsByNumeroContrat(c.getNumeroContrat())) {
            throw new BusinessException("Le numéro " + c.getNumeroContrat() + " existe déjà.");
        }
        if (c.getClient() == null) throw new BusinessException("Le client est obligatoire.");
        validerChamps(c);
        c.setStatut(StatutContrat.EN_ATTENTE);
        return contratRepository.save(c);
    }

    @Transactional
    public Contrat modifier(Long id, LocalDate dateDebut, LocalDate dateFin, BigDecimal prime,
                            TypeAssurance type, List<Garantie> garanties) {
        Contrat c = mustExist(id);
        StatutContrat s = c.getStatut();
        if (s == StatutContrat.RESILIE || s == StatutContrat.EXPIRE)
            throw new BusinessException("Un contrat " + s + " ne peut plus être modifié.");
        c.setDateDebut(dateDebut); c.setDateFin(dateFin); c.setPrime(prime);
        c.setTypeAssurance(type); c.setGaranties(garanties);
        validerChamps(c);
        return contratRepository.save(c);
    }

    private void validerChamps(Contrat c) {
        if (c.getDateDebut() == null) throw new BusinessException("La date de début est obligatoire.");
        if (c.getDateFin() != null && !c.getDateFin().isAfter(c.getDateDebut()))
            throw new BusinessException("La date de fin doit être postérieure à la date de début.");
        if (c.getPrime() == null || c.getPrime().signum() <= 0)
            throw new BusinessException("La prime est obligatoire et doit être strictement positive.");
        if (c.getTypeAssurance() == null) throw new BusinessException("Le type d'assurance est obligatoire.");
    }

    @Transactional
    public Contrat activer(Long contratId) {
        Contrat c = mustExist(contratId); exiger(c, "activer", StatutContrat.EN_ATTENTE, StatutContrat.SUSPENDU);
        c.setStatut(StatutContrat.ACTIF); return contratRepository.save(c);
    }

    @Transactional
    public Contrat suspendre(Long contratId) {
        Contrat c = mustExist(contratId); exiger(c, "suspendre", StatutContrat.ACTIF);
        c.setStatut(StatutContrat.SUSPENDU); return contratRepository.save(c);
    }

    @Transactional
    public Contrat resilier(Long contratId) {
        Contrat c = mustExist(contratId); exiger(c, "résilier", StatutContrat.ACTIF, StatutContrat.SUSPENDU);
        c.setStatut(StatutContrat.RESILIE); return contratRepository.save(c);
    }

    @Transactional
    public Contrat renouveler(Long contratId, int dureeMois) {
        if (dureeMois <= 0) throw new BusinessException("La durée de renouvellement doit être positive.");
        Contrat c = mustExist(contratId); exiger(c, "renouveler", StatutContrat.ACTIF, StatutContrat.EXPIRE);
        LocalDate base = c.getDateFin() == null || c.getDateFin().isBefore(LocalDate.now()) ? LocalDate.now() : c.getDateFin();
        c.setDateFin(base.plusMonths(dureeMois)); c.setStatut(StatutContrat.ACTIF);
        return contratRepository.save(c);
    }

    @Transactional
    public int mettreAJourContratsExpires() {
        return contratRepository.mettreAJourContratsExpires(StatutContrat.EXPIRE, StatutContrat.ACTIF, LocalDate.now());
    }

    private Contrat mustExist(Long id) {
        if (id == null) throw new BusinessException("Contrat introuvable : " + id);
        return contratRepository.findCompletById(id).orElseThrow(() -> new BusinessException("Contrat introuvable : " + id));
    }

    private void exiger(Contrat c, String action, StatutContrat... autorises) {
        for (StatutContrat s : autorises) if (c.getStatut() == s) return;
        throw new BusinessException("Impossible de " + action + " le contrat " + c.getNumeroContrat()
                + " (statut courant : " + c.getStatut() + ").");
    }
}
