package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.Garantie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table {@code garanties}.
 */
@Repository
public interface GarantieRepository extends JpaRepository<Garantie, Long> {

    Optional<Garantie> findByNom(String nom);

    boolean existsByNom(String nom);

    List<Garantie> findByNomContainingIgnoreCase(String fragment);
}
