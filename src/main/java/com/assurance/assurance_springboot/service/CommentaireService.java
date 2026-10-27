package com.assurance.assurance_springboot.service;

import com.assurance.assurance_springboot.model.*;
import com.assurance.assurance_springboot.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentaireService {

    private final CommentaireRepository commentaires;
    private final SinistreRepository sinistres;
    private final UtilisateurRepository utilisateurs;

    public CommentaireService(
            CommentaireRepository c,
            SinistreRepository s,
            UtilisateurRepository u
    ) {
        commentaires = c;
        sinistres = s;
        utilisateurs = u;
    }

    public Commentaire ajouter(Long sinistreId, Long auteurId, String texte) {
        if (texte == null || texte.isBlank() || texte.trim().length() > 2000) {
            throw new BusinessException(
                    "Le commentaire est obligatoire (2000 caractères max)."
            );
        }

        Sinistre s = sinistres.findById(sinistreId)
                .orElseThrow(() -> new BusinessException("Sinistre introuvable."));

        Utilisateur u = utilisateurs.findById(auteurId)
                .orElseThrow(() -> new BusinessException("Auteur introuvable."));

        if (u.getRole() != Role.AGENT && u.getRole() != Role.ADMIN) {
            throw new BusinessException(
                    "Seul un agent peut commenter un sinistre."
            );
        }

        Commentaire c = new Commentaire();
        c.setSinistre(s);
        c.setAuteur(u);
        c.setTexte(texte.trim());

        return commentaires.save(c);
    }

    @Transactional(readOnly = true)
    public List<Commentaire> listerParSinistre(Long id) {
        return commentaires.findBySinistreIdOrderByDateCreationAsc(id);
    }
}