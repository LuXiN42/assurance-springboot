package com.assurance.assurance_springboot.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assurance.assurance_springboot.model.Garantie;
import com.assurance.assurance_springboot.model.TypeAssurance;
import com.assurance.assurance_springboot.repository.ContratRepository;
import com.assurance.assurance_springboot.repository.GarantieRepository;
import com.assurance.assurance_springboot.repository.TypeAssuranceRepository;

@Service
@Transactional
public class TypeAssuranceService {

    private final TypeAssuranceRepository repo;
    private final ContratRepository contratRepo;
    private final GarantieRepository garantieRepo;

    public TypeAssuranceService(TypeAssuranceRepository repo,
                                ContratRepository contratRepo,
                                GarantieRepository garantieRepo) {
        this.repo = repo;
        this.contratRepo = contratRepo;
        this.garantieRepo = garantieRepo;
    }

    @Transactional(readOnly = true)
    public List<TypeAssurance> lister() {
        return repo.findAll(Sort.by("nom"));
    }

    @Transactional(readOnly = true)
    public List<Garantie> listerGaranties() {
        return garantieRepo.findAll(Sort.by("nom"));
    }

    @Transactional(readOnly = true)
    public TypeAssurance trouver(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new BusinessException("Type d'assurance introuvable."));
    }

    public void enregistrer(Long id, String nom, String description, BigDecimal prixBase,
                            List<Long> garantieIds) {
        if (nom == null || nom.isBlank())
            throw new BusinessException("Le nom est obligatoire.");
        if (prixBase == null || prixBase.signum() < 0)
            throw new BusinessException("Le prix de base doit être positif ou nul.");

        TypeAssurance t = (id == null) ? new TypeAssurance() : trouver(id);
        t.setNom(nom.trim());
        t.setDescription(description);
        t.setPrixBase(prixBase);
        t.setGarantiesDisponibles(garantieIds == null
                ? new HashSet<>()
                : new HashSet<>(garantieRepo.findAllById(garantieIds)));
        repo.save(t);
    }

    public void supprimer(Long id) {
        if (contratRepo.existsByTypeAssuranceId(id))
            throw new BusinessException("Suppression impossible : des contrats utilisent ce type.");
        repo.deleteById(id);
    }
}