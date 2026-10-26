package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.Garantie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Accès aux données des garanties. */
@Repository
public interface GarantieRepository extends JpaRepository<Garantie, Long> {
    Optional<Garantie> findByNom(String nom);
    boolean existsByNom(String nom);
    List<Garantie> findByNomContainingIgnoreCase(String fragment);

    @Query("select g from Contrat c join c.garanties g where c.id = :contratId order by g.nom")
    List<Garantie> findByContratIdOrderByNom(@Param("contratId") Long contratId);
}
