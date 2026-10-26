package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.PieceJustificative;
import com.assurance.assurance_springboot.model.Sinistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table {@code pieces_justificatives}.
 */
@Repository
public interface PieceJustificativeRepository extends JpaRepository<PieceJustificative, Long> {

    List<PieceJustificative> findBySinistre(Sinistre sinistre);

    List<PieceJustificative> findBySinistreId(Long sinistreId);

    Optional<PieceJustificative> findByNomStocke(String nomStocke);

    boolean existsByNomStocke(String nomStocke);

    long countBySinistreId(Long sinistreId);
}
