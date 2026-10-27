package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.PieceJustificative;
import com.assurance.assurance_springboot.model.Sinistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PieceJustificativeRepository extends JpaRepository<PieceJustificative, Long> {

    List<PieceJustificative> findBySinistre(Sinistre sinistre);

    List<PieceJustificative> findBySinistreId(Long sinistreId);

    Optional<PieceJustificative> findByNomStocke(String nomStocke);

    boolean existsByNomStocke(String nomStocke);

    long countBySinistreId(Long sinistreId);
    @Query("""
            SELECT p FROM PieceJustificative p
            WHERE p.id = :pieceId
            AND p.sinistre.contrat.client.id = :clientId
            """)
    Optional<PieceJustificative> findOwnedById(@Param("pieceId") Long pieceId,
                                            @Param("clientId") Long clientId);
}
