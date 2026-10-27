package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.Paiement;
import com.assurance.assurance_springboot.model.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "contrat",
            "contrat.client",
            "contrat.typeAssurance"
    })
    List<Paiement> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "contrat",
            "contrat.client",
            "contrat.typeAssurance"
    })
    Optional<Paiement> findById(Long id);

    @EntityGraph(attributePaths = {
            "contrat",
            "contrat.client",
            "contrat.typeAssurance"
    })
    List<Paiement> findByContrat(Contrat contrat);

    List<Paiement> findByContratId(Long contratId);

    @EntityGraph(attributePaths = {
            "contrat",
            "contrat.client",
            "contrat.typeAssurance"
    })
    List<Paiement> findByContratIdOrderByDateDesc(Long contratId);

    @EntityGraph(attributePaths = {
            "contrat",
            "contrat.client",
            "contrat.typeAssurance"
    })
    List<Paiement> findByStatut(StatutPaiement statut);
    @EntityGraph(attributePaths={"contrat", "contrat.client", "contrat.typeAssurance"})
    @Query("select p from Paiement p where p.statut=:statut and p.date between :debut and :fin order by p.date asc")
    List<Paiement> findEcheancesGlobales(@Param("statut") StatutPaiement statut, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    List<Paiement> findByStatutAndContratId(
            StatutPaiement statut,
            Long contratId
    );

    long countByStatut(StatutPaiement statut);

    @EntityGraph(attributePaths = {
            "contrat",
            "contrat.client",
            "contrat.typeAssurance"
    })
    List<Paiement> findByContratClientIdOrderByDateDesc(Long clientId);

    @EntityGraph(attributePaths = {
            "contrat",
            "contrat.client",
            "contrat.typeAssurance"
    })
    List<Paiement> findByContratClientIdAndStatutOrderByDateDesc(
            Long clientId,
            StatutPaiement statut
    );

    /**
     * Échéances EN_ATTENTE dans une période pour un client.
     */
    @EntityGraph(attributePaths = {
            "contrat",
            "contrat.client",
            "contrat.typeAssurance"
    })
    @Query("select p from Paiement p "
            + "where p.contrat.client.id = :clientId "
            + "and p.statut = :statut "
            + "and p.date between :debut and :fin "
            + "order by p.date asc")
    List<Paiement> findEcheances(
            @Param("clientId") Long clientId,
            @Param("statut") StatutPaiement statut,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );

    @Query("select coalesce(sum(p.montant), 0) "
            + "from Paiement p where p.contrat.id = :contratId")
    BigDecimal sumMontantByContratId(
            @Param("contratId") Long contratId
    );

    @Query("select coalesce(sum(p.montant), 0) "
            + "from Paiement p "
            + "where p.contrat.id = :contratId and p.statut = :statut")
    BigDecimal sumMontantByContratIdAndStatut(
            @Param("contratId") Long contratId,
            @Param("statut") StatutPaiement statut
    );

    /**
     * Total des paiements pour un statut donné.
     */
    @Query("select coalesce(sum(p.montant), 0) "
            + "from Paiement p where p.statut = :statut")
    BigDecimal sommeParStatut(
            @Param("statut") StatutPaiement statut
    );

    /**
     * Total des paiements pour un statut et une période inclusivement.
     */
    @Query("select coalesce(sum(p.montant), 0) "
            + "from Paiement p "
            + "where p.statut = :statut "
            + "and p.date between :debut and :fin")
    BigDecimal sommeParStatutEtPeriode(
            @Param("statut") StatutPaiement statut,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );
}