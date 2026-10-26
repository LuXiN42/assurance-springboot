package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.Paiement;
import com.assurance.assurance_springboot.model.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Accès aux données de la table {@code paiements}.
 */
@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findByContrat(Contrat contrat);

    List<Paiement> findByContratId(Long contratId);

    List<Paiement> findByContratIdOrderByDateDesc(Long contratId);

    List<Paiement> findByStatut(StatutPaiement statut);

    List<Paiement> findByStatutAndContratId(StatutPaiement statut, Long contratId);

    long countByStatut(StatutPaiement statut);

    /**
     * Somme des montants payés pour un contrat donné.
     */
    @Query("select coalesce(sum(p.montant), 0) from Paiement p where p.contrat.id = :contratId")
    BigDecimal sumMontantByContratId(@Param("contratId") Long contratId);

    /**
     * Somme des montants effectivement réglés (statut EFFECTUE) pour un contrat donné.
     */
    @Query("select coalesce(sum(p.montant), 0) from Paiement p "
            + "where p.contrat.id = :contratId and p.statut = :statut")
    BigDecimal sumMontantByContratIdAndStatut(@Param("contratId") Long contratId,
                                              @Param("statut") StatutPaiement statut);
}
