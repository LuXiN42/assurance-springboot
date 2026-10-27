package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.Sinistre;
import com.assurance.assurance_springboot.model.StatutSinistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import com.assurance.assurance_springboot.model.Contrat;
import java.util.List;
import java.util.Optional;

@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Long> {
    @EntityGraph(attributePaths={"contrat", "contrat.client", "contrat.typeAssurance"})
    @Query("select distinct s from Sinistre s") List<Sinistre> findAllComplets();
    @EntityGraph(attributePaths={"contrat", "contrat.client", "contrat.typeAssurance"})
    @Query("select distinct s from Sinistre s where s.statut = :statut") List<Sinistre> findByStatutComplet(@Param("statut") StatutSinistre statut);
    @EntityGraph(attributePaths={"contrat", "contrat.client", "contrat.typeAssurance"})
    @Query("select distinct s from Sinistre s where s.id = :id") Optional<Sinistre> findCompletById(@Param("id") Long id);
    Optional<Sinistre> findByNumeroSinistre(String numeroSinistre);
    boolean existsByNumeroSinistre(String numeroSinistre);
    List<Sinistre> findByContrat(Contrat contrat);
    List<Sinistre> findByContratId(Long contratId);
    List<Sinistre> findByContratIdOrderByDateDesc(Long contratId);
    List<Sinistre> findByContratClientId(Long clientId);
    @EntityGraph(attributePaths={"contrat", "contrat.client", "contrat.typeAssurance"})
    List<Sinistre> findByContratClientIdOrderByDateDesc(Long clientId);
    @EntityGraph(attributePaths={"contrat", "contrat.client", "contrat.typeAssurance"})
    List<Sinistre> findByStatut(StatutSinistre statut);
    List<Sinistre> findByStatutAndContratId(StatutSinistre statut, Long contratId);
    long countByStatut(StatutSinistre statut);
    long countByContratId(Long contratId);
    @Query("select s.statut, count(s) from Sinistre s group by s.statut")
    List<Object[]> compterParStatut();
    @Query("select coalesce(sum(s.montantEstime), 0) from Sinistre s")
    BigDecimal sommeMontantsEstimes();
    @Query("select coalesce(sum(s.montantIndemnisation), 0) from Sinistre s")
    BigDecimal sommeIndemnisations();
}
