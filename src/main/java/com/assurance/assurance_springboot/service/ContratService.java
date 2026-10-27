package com.assurance.assurance_springboot.service;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.StatutContrat;
import com.assurance.assurance_springboot.repository.ContratRepository;
import com.assurance.assurance_springboot.repository.TypeAssuranceRepository;
import com.assurance.assurance_springboot.repository.UtilisateurRepository;
import com.assurance.assurance_springboot.model.Role;
import com.assurance.assurance_springboot.model.TypeAssurance;
import com.assurance.assurance_springboot.model.Utilisateur;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ContratService {

    private final ContratRepository repo;
    private final UtilisateurRepository utilisateurs;
    private final TypeAssuranceRepository types;

    public ContratService(
            ContratRepository repo,
            UtilisateurRepository utilisateurs,
            TypeAssuranceRepository types
    ) {
        this.repo = repo;
        this.utilisateurs = utilisateurs;
        this.types = types;
    }

    @Transactional(readOnly = true)
    public List<Contrat> lister() {
        return repo.findAllComplets();
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> clients() {
        return utilisateurs.findByRole(Role.CLIENT);
    }

    @Transactional(readOnly = true)
    public List<TypeAssurance> types() {
        return types.findAll();
    }

    @Transactional(readOnly = true)
    public Contrat trouver(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new BusinessException("Contrat introuvable : " + id)
                );
    }

    public Contrat enregistrer(
            Long id,
            String numero,
            LocalDate debut,
            LocalDate fin,
            BigDecimal prime,
            StatutContrat statut,
            Long clientId,
            Long typeId
    ) {
        if (numero == null || numero.isBlank() || numero.trim().length() > 80) {
            throw new BusinessException(
                    "Numéro de contrat obligatoire (80 caractères max)."
            );
        }

        numero = numero.trim();

        boolean duplicate = id == null
                ? repo.existsByNumeroContrat(numero)
                : repo.existsByNumeroContratAndIdNot(numero, id);

        if (duplicate) {
            throw new BusinessException("Ce numéro de contrat existe déjà.");
        }

        if (debut == null) {
            throw new BusinessException("La date de début est obligatoire.");
        }

        if (fin != null && !fin.isAfter(debut)) {
            throw new BusinessException(
                    "La date de fin doit être postérieure à la date de début."
            );
        }

        if (prime == null || prime.signum() <= 0) {
            throw new BusinessException("La prime doit être strictement positive.");
        }

        if (statut == null) {
            throw new BusinessException("Le statut du contrat est obligatoire.");
        }

        Utilisateur client = utilisateurs.findById(clientId)
                .orElseThrow(() -> new BusinessException("Client introuvable."));

        if (client.getRole() != Role.CLIENT) {
            throw new BusinessException(
                    "Le titulaire doit avoir le rôle CLIENT."
            );
        }

        TypeAssurance type = types.findById(typeId)
                .orElseThrow(() ->
                        new BusinessException("Type d'assurance introuvable.")
                );

        Contrat c = id == null ? new Contrat() : trouver(id);

        if (c.getStatut() == StatutContrat.RESILIE
                || c.getStatut() == StatutContrat.EXPIRE) {
            if (id != null) {
                throw new BusinessException(
                        "Un contrat résilié ou expiré ne peut pas être modifié ici."
                );
            }
        }

        if (id != null
                && c.getStatut() != statut
                && !transitionAutorisee(c.getStatut(), statut)) {
            throw new BusinessException(
                    "Transition de statut invalide : "
                            + c.getStatut() + " vers " + statut + "."
            );
        }

        c.setNumeroContrat(numero);
        c.setDateDebut(debut);
        c.setDateFin(fin);
        c.setPrime(prime);
        c.setStatut(statut);
        c.setClient(client);
        c.setTypeAssurance(type);

        return repo.save(c);
    }

    private boolean transitionAutorisee(
            StatutContrat courant,
            StatutContrat cible
    ) {
        return switch (courant) {
            case EN_ATTENTE -> cible == StatutContrat.ACTIF;
            case ACTIF -> cible == StatutContrat.SUSPENDU
                    || cible == StatutContrat.RESILIE
                    || cible == StatutContrat.EXPIRE;
            case SUSPENDU -> cible == StatutContrat.ACTIF
                    || cible == StatutContrat.RESILIE;
            case EXPIRE -> cible == StatutContrat.ACTIF;
            case RESILIE -> false;
        };
    }

    @Transactional(readOnly = true)
    public List<Contrat> rechercher(
            String numero,
            String nomClient,
            Long typeId,
            StatutContrat statut
    ) {
        return repo.rechercher(vide(numero), vide(nomClient), typeId, statut);
    }

    @Transactional(readOnly = true)
    public Contrat trouverComplet(Long id) {
        return repo.findCompletById(id)
                .orElseThrow(() ->
                        new BusinessException("Contrat introuvable : " + id)
                );
    }

    public Contrat suspendre(Long id) {
        return changerStatut(id, StatutContrat.SUSPENDU);
    }

    public Contrat resilier(Long id) {
        return changerStatut(id, StatutContrat.RESILIE);
    }

    public Contrat reactiver(Long id) {
        return changerStatut(id, StatutContrat.ACTIF);
    }

    private Contrat changerStatut(Long id, StatutContrat cible) {
        Contrat c = trouver(id);

        if (c.getStatut() != cible
                && !transitionAutorisee(c.getStatut(), cible)) {
            throw new BusinessException(
                    "Transition de statut invalide : "
                            + c.getStatut() + " vers " + cible + "."
            );
        }

        c.setStatut(cible);
        return repo.save(c);
    }

    public Contrat renouveler(Long id, LocalDate nouvelleDateFin) {
        Contrat c = trouver(id);

        if (c.getStatut() != StatutContrat.EXPIRE) {
            throw new BusinessException(
                    "Seul un contrat expiré peut être renouvelé."
            );
        }

        if (nouvelleDateFin == null
                || c.getDateDebut() == null
                || !nouvelleDateFin.isAfter(c.getDateDebut())
                || (c.getDateFin() != null
                && !nouvelleDateFin.isAfter(c.getDateFin()))) {
            throw new BusinessException(
                    "La nouvelle date de fin doit dépasser la date de fin actuelle."
            );
        }

        c.setDateFin(nouvelleDateFin);
        c.setStatut(StatutContrat.ACTIF);

        return repo.save(c);
    }

    private String vide(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    public void supprimer(Long id) {
        Contrat c = trouver(id);

        if (c.getSinistres() != null && !c.getSinistres().isEmpty()) {
            throw new BusinessException(
                    "Suppression impossible : le contrat possède des sinistres."
            );
        }

        repo.delete(c);
    }
}