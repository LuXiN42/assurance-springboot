package com.assurance.assurance_springboot.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assurance.assurance_springboot.repository.TypeAssuranceRepository;
import com.assurance.assurance_springboot.repository.ContratRepository;
import com.assurance.assurance_springboot.model.TypeAssurance;

@Service
@Transactional
public class TypeAssuranceService {

    private final TypeAssuranceRepository repo;
    private final ContratRepository contratRepo;

    public TypeAssuranceService(TypeAssuranceRepository repo, ContratRepository contratRepo) {
        this.repo = repo;
        this.contratRepo = contratRepo;
    }

    @Transactional(readOnly = true)
    public List<TypeAssurance> lister() {
        return repo.findAll(Sort.by("nom"));
    }

    @Transactional(readOnly = true)
    public TypeAssurance trouver(Long id) {
        return repo.findById(id).orElseThrow(() -> new BusinessException("Type introuvable."));
    }

    public void enregistrer(Long id, String nom, String description, BigDecimal prixBase) {
        if (nom == null || nom.isBlank() || nom.length() > 150)
            throw new BusinessException("Le nom est obligatoire (150 caractères max).");
        if (prixBase == null || prixBase.signum() < 0)
            throw new BusinessException("Le prix de base doit être positif ou nul.");
        nom = nom.trim();
        boolean doublon = (id == null)
                ? repo.existsByNomIgnoreCase(nom)
                : repo.existsByNomIgnoreCaseAndIdNot(nom, id);
        if (doublon) throw new BusinessException("Ce type d'assurance existe déjà.");

        TypeAssurance t = (id == null) ? new TypeAssurance() : trouver(id);
        t.setNom(nom);
        t.setDescription(description);
        t.setPrixBase(prixBase);
        repo.save(t);
    }

    public void supprimer(Long id) {
        if (contratRepo.existsByTypeAssuranceId(id))
            throw new BusinessException("Suppression impossible : des contrats utilisent ce type.");
        repo.deleteById(id);
    }
}