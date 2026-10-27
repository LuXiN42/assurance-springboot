package com.assurance.assurance_springboot.service;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assurance.assurance_springboot.repository.UtilisateurRepository;
import com.assurance.assurance_springboot.repository.ContratRepository;
import com.assurance.assurance_springboot.model.Utilisateur;
import com.assurance.assurance_springboot.model.Role;


@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository repo;
    private final ContratRepository contratRepo;
    private final PasswordEncoder encoder;

    public UtilisateurService(UtilisateurRepository repo, ContratRepository contratRepo,
                              PasswordEncoder encoder) {
        this.repo = repo;
        this.contratRepo = contratRepo;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> lister() {
        return repo.findAllByOrderByNomAscPrenomAsc();
    }

    @Transactional(readOnly = true)
    public Utilisateur trouver(Long id) {
        return repo.findById(id).orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
    }

    public void enregistrer(Long id, String nom, String prenom, String email, String telephone,
                            String adresse, String motDePasse, Role role) {
        if (nom == null || nom.isBlank() || nom.length() > 100)
            throw new BusinessException("Le nom est obligatoire (100 caractères max).");
        if (prenom == null || prenom.isBlank() || prenom.length() > 100)
            throw new BusinessException("Le prénom est obligatoire (100 caractères max).");
        if (email == null || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new BusinessException("Email invalide.");
        if (telephone != null && telephone.length() > 20)
            throw new BusinessException("Téléphone trop long (20 caractères max).");
        if (adresse != null && adresse.length() > 255)
            throw new BusinessException("Adresse trop longue (255 caractères max).");
        if (role == null) throw new BusinessException("Le rôle est obligatoire.");

        email = email.trim().toLowerCase();
        boolean doublon = (id == null)
                ? repo.existsByEmailIgnoreCase(email)
                : repo.existsByEmailIgnoreCaseAndIdNot(email, id);
        if (doublon) throw new BusinessException("Cet email est déjà utilisé.");

        Utilisateur u;
        if (id == null) {
            if (motDePasse == null || motDePasse.length() < 8)
                throw new BusinessException("Mot de passe obligatoire (8 caractères minimum).");
            u = new Utilisateur();
            u.setPasswordHash(encoder.encode(motDePasse));
        } else {
            u = trouver(id);
            if (u.getRole() != role && contratRepo.existsByClientId(id))
                throw new BusinessException(
                    "Impossible de changer le rôle : cet utilisateur possède des contrats.");
            if (motDePasse != null && !motDePasse.isBlank()) {
                if (motDePasse.length() < 8)
                    throw new BusinessException("Mot de passe : 8 caractères minimum.");
                u.setPasswordHash(encoder.encode(motDePasse));
            }
        }
        u.setNom(nom.trim());
        u.setPrenom(prenom.trim());
        u.setEmail(email);
        u.setTelephone(telephone == null || telephone.isBlank() ? null : telephone.trim());
        u.setAdresse(adresse == null || adresse.isBlank() ? null : adresse.trim());
        u.setRole(role);
        repo.save(u);
    }

    public void supprimer(Long id, String emailConnecte) {
        Utilisateur u = trouver(id);
        if (u.getEmail().equalsIgnoreCase(emailConnecte))
            throw new BusinessException("Vous ne pouvez pas supprimer votre propre compte.");
        if (contratRepo.existsByClientId(id))
            throw new BusinessException("Suppression impossible : cet utilisateur a des contrats.");
        repo.delete(u);
    }
}