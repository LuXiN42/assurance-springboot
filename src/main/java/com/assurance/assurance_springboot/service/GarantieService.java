package com.assurance.assurance_springboot.service;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.Garantie;
import com.assurance.assurance_springboot.model.StatutContrat;
import com.assurance.assurance_springboot.repository.ContratRepository;
import com.assurance.assurance_springboot.repository.GarantieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GarantieService {
    private final GarantieRepository garantieRepository;
    private final ContratRepository contratRepository;

    public GarantieService(GarantieRepository garantieRepository, ContratRepository contratRepository) {
        this.garantieRepository = garantieRepository; this.contratRepository = contratRepository;
    }

    public List<Garantie> lister() { return garantieRepository.findAll(); }

    public Garantie trouver(Long id) {
        if (id == null) throw new BusinessException("Garantie introuvable : " + id);
        return garantieRepository.findById(id).orElseThrow(() -> new BusinessException("Garantie introuvable : " + id));
    }

    @Transactional
    public Garantie creer(String nom, String description) {
        String n = valider(nom, description);
        if (garantieRepository.findByNom(n).isPresent()) throw new BusinessException("Une garantie « " + n + " » existe déjà.");
        Garantie g = new Garantie(); g.setNom(n); g.setDescription(vide(description) ? null : description.trim());
        return garantieRepository.save(g);
    }

    @Transactional
    public Garantie modifier(Long id, String nom, String description) {
        String n = valider(nom, description); Garantie g = trouver(id);
        Garantie autre = garantieRepository.findByNom(n).orElse(null);
        if (autre != null && !autre.getId().equals(id)) throw new BusinessException("Une autre garantie « " + n + " » existe déjà.");
        g.setNom(n); g.setDescription(vide(description) ? null : description.trim());
        return garantieRepository.save(g);
    }

    @Transactional
    public void supprimer(Long id) {
        Garantie g = trouver(id);
        if (contratRepository.existsByGaranties_Id(id)) throw new BusinessException("Impossible de supprimer « " + g.getNom()
                + " » : elle est associée à au moins un contrat.");
        garantieRepository.delete(g);
    }

    public List<Garantie> garantiesDuContrat(Long contratId) {
        return garantieRepository.findByContratIdOrderByNom(contratId);
    }

    @Transactional
    public void associer(Long garantieId, Long contratId) {
        Contrat c = contratModifiable(contratId); Garantie g = trouver(garantieId);
        for (Garantie existante : c.getGaranties()) if (existante.getId().equals(garantieId))
            throw new BusinessException("Cette garantie est déjà associée au contrat.");
        c.getGaranties().add(g); contratRepository.save(c);
    }

    @Transactional
    public void dissocier(Long garantieId, Long contratId) {
        Contrat c = contratModifiable(contratId);
        c.getGaranties().removeIf(g -> g.getId().equals(garantieId));
        contratRepository.save(c);
    }

    private Contrat contratModifiable(Long contratId) {
        Contrat c = contratId == null ? null : contratRepository.findByIdAvecGaranties(contratId).orElse(null);
        if (c == null) throw new BusinessException("Contrat introuvable : " + contratId);
        if (c.getStatut() == StatutContrat.RESILIE || c.getStatut() == StatutContrat.EXPIRE)
            throw new BusinessException("Un contrat " + c.getStatut() + " ne peut plus être modifié.");
        return c;
    }

    private String valider(String nom, String description) {
        if (vide(nom)) throw new BusinessException("Le nom de la garantie est obligatoire.");
        String n = nom.trim();
        if (n.length() > 150) throw new BusinessException("Le nom ne doit pas dépasser 150 caractères.");
        if (description != null && description.length() > 2000) throw new BusinessException("La description ne doit pas dépasser 2000 caractères.");
        return n;
    }
    private static boolean vide(String s) { return s == null || s.isBlank(); }
}
