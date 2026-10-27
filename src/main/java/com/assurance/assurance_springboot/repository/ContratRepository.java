package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.StatutContrat;
import com.assurance.assurance_springboot.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données des contrats.
 */
@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {

    @EntityGraph(attributePaths = {"client", "typeAssurance", "garanties"})
    @Query("select distinct c from Contrat c order by c.dateDebut desc")
    List<Contrat> findAllComplets();

    Optional<Contrat> findByNumeroContrat(String numeroContrat);

    boolean existsByNumeroContrat(String numeroContrat);

    List<Contrat> findByClient(Utilisateur client);

    List<Contrat> findByClientId(Long clientId);

    @EntityGraph(attributePaths={"client", "typeAssurance", "garanties"})
    List<Contrat> findByClientIdOrderByDateDebutDesc(Long clientId);

    List<Contrat> findByStatut(StatutContrat statut);

    List<Contrat> findByStatutAndClientId(
            StatutContrat statut,
            Long clientId
    );

    List<Contrat> findByTypeAssuranceId(Long typeAssuranceId);

    List<Contrat> findByDateDebutBetween(
            LocalDate debut,
            LocalDate fin
    );

    List<Contrat> findByDateFinBefore(LocalDate date);

    long countByStatut(StatutContrat statut);

    long countByClientId(Long clientId);

    long countByDateDebutBetween(LocalDate debut, LocalDate fin);

    /**
     * Recherche multicritère des contrats.
     */
    @EntityGraph(attributePaths = {"client", "typeAssurance", "garanties"})
    @Query("select distinct c from Contrat c "
            + "where (:numero is null or lower(c.numeroContrat) "
            + "like lower(concat('%', :numero, '%'))) "
            + "and (:nomClient is null or lower(coalesce(c.client.nom, '')) "
            + "like lower(concat('%', :nomClient, '%'))) "
            + "and (:typeId is null or c.typeAssurance.id = :typeId) "
            + "and (:statut is null or c.statut = :statut) "
            + "order by c.dateDebut desc")
    List<Contrat> rechercher(
            @Param("numero") String numero,
            @Param("nomClient") String nomClient,
            @Param("typeId") Long typeId,
            @Param("statut") StatutContrat statut
    );

    @EntityGraph(attributePaths = {"client", "typeAssurance", "garanties"})
    @Query("select distinct c from Contrat c where c.id = :id")
    Optional<Contrat> findCompletById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"garanties"})
    @Query("select c from Contrat c where c.id = :id")
    Optional<Contrat> findByIdAvecGaranties(@Param("id") Long id);

    /**
     * Contrat limité au client propriétaire.
     */
    @Query("select c from Contrat c "
            + "where c.id = :contratId and c.client.id = :clientId")
    Optional<Contrat> findByIdEtClient(
            @Param("contratId") Long contratId,
            @Param("clientId") Long clientId
    );

    boolean existsByGaranties_Id(Long garantieId);

    /**
     * Passe en EXPIRE les contrats actifs dont la date de fin est dépassée.
     */
    @Modifying
    @Query("update Contrat c set c.statut = :expire "
            + "where c.dateFin < :today and c.statut = :actif")
    int mettreAJourContratsExpires(
            @Param("expire") StatutContrat expire,
            @Param("actif") StatutContrat actif,
            @Param("today") LocalDate today
    );

    boolean existsByNumeroContratAndIdNot(
            String numeroContrat,
            Long id
    );

    boolean existsByClientId(Long clientId);

    boolean existsByTypeAssuranceId(Long typeAssuranceId);

    /**
     * Nombre de contrats par type d'assurance.
     */
    @Query("select t.id, t.nom, count(c) "
            + "from Contrat c join c.typeAssurance t "
            + "group by t.id, t.nom order by t.nom")
    List<Object[]> compterParType();

    /**
     * Nombre de clients distincts ayant un contrat par type d'assurance.
     * Un client peut apparaître dans plusieurs types s'il possède
     * des contrats de types différents.
     */
    @Query("select t.id, t.nom, count(distinct c.client.id) "
            + "from Contrat c join c.typeAssurance t "
            + "group by t.id, t.nom order by t.nom")
    List<Object[]> compterClientsDistinctsParType();

    @Query("select sum(c.prime) from Contrat c")
    BigDecimal sommePrimes();
}