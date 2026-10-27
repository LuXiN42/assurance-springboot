package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.Commentaire;
import com.assurance.assurance_springboot.model.Sinistre;
import com.assurance.assurance_springboot.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {

    List<Commentaire> findBySinistre(Sinistre sinistre);

    List<Commentaire> findBySinistreId(Long sinistreId);

    @EntityGraph(attributePaths = {"auteur"})
    List<Commentaire> findBySinistreIdOrderByDateCreationAsc(Long sinistreId);

    List<Commentaire> findByAuteur(Utilisateur auteur);

    List<Commentaire> findByAuteurIdOrderByDateCreationDesc(Long auteurId);

    long countBySinistreId(Long sinistreId);
}
