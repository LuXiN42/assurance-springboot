package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.StatutContrat;
import com.assurance.assurance_springboot.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table {@code contrats}.
 */
@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {

    Optional<Contrat> findByNumeroContrat(String numeroContrat);

    boolean existsByNumeroContrat(String numeroContrat);

    List<Contrat> findByClient(Utilisateur client);

    List<Contrat> findByClientId(Long clientId);

    List<Contrat> findByClientIdOrderByDateDebutDesc(Long clientId);

    List<Contrat> findByStatut(StatutContrat statut);

    List<Contrat> findByStatutAndClientId(StatutContrat statut, Long clientId);

    List<Contrat> findByTypeAssuranceId(Long typeAssuranceId);

    List<Contrat> findByDateDebutBetween(LocalDate debut, LocalDate fin);

    List<Contrat> findByDateFinBefore(LocalDate date);

    long countByStatut(StatutContrat statut);

    long countByClientId(Long clientId);
}
