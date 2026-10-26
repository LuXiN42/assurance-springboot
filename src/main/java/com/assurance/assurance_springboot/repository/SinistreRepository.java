package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.Sinistre;
import com.assurance.assurance_springboot.model.StatutSinistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table {@code sinistres}.
 */
@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Long> {

    Optional<Sinistre> findByNumeroSinistre(String numeroSinistre);

    boolean existsByNumeroSinistre(String numeroSinistre);

    List<Sinistre> findByContrat(Contrat contrat);

    List<Sinistre> findByContratId(Long contratId);

    List<Sinistre> findByContratIdOrderByDateDesc(Long contratId);

    List<Sinistre> findByContratClientId(Long clientId);

    List<Sinistre> findByContratClientIdOrderByDateDesc(Long clientId);

    List<Sinistre> findByStatut(StatutSinistre statut);

    List<Sinistre> findByStatutAndContratId(StatutSinistre statut, Long contratId);

    long countByStatut(StatutSinistre statut);

    long countByContratId(Long contratId);
}
