package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.TypeAssurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table {@code types_assurance}.
 */
@Repository
public interface TypeAssuranceRepository extends JpaRepository<TypeAssurance, Long> {

    Optional<TypeAssurance> findByNom(String nom);
    boolean existsByNom(String nom);
    List<TypeAssurance> findByNomContainingIgnoreCase(String fragment);
    List<TypeAssurance> findByNomContainingIgnoreCaseOrderByNomAsc(String fragment);
    boolean existsByNomIgnoreCase(String nom);
    boolean existsByNomIgnoreCaseAndIdNot(String nom, Long id);
}
