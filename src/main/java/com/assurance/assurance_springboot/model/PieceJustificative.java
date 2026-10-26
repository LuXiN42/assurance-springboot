package com.assurance.assurance_springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entité {@code pieces_justificatives}.
 */
@Entity
@Table(name = "pieces_justificatives",
        uniqueConstraints = @UniqueConstraint(name = "uk_pj_nom_stocke", columnNames = "nom_stocke"))
public class PieceJustificative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "nom_original", nullable = false, length = 255)
    private String nomOriginal;

    @NotNull
    @Size(min = 1, max = 300)
    @Column(name = "nom_stocke", nullable = false, length = 300)
    private String nomStocke;

    @Size(max = 100)
    @Column(name = "type_mime", length = 100)
    private String typeMime;

    @PositiveOrZero
    @Column(name = "taille", nullable = false)
    private long taille;

    @NotNull
    @Column(name = "date_depot", nullable = false)
    private LocalDateTime dateDepot = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sinistre_id", nullable = false)
    private Sinistre sinistre;

    public PieceJustificative() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomOriginal() {
        return nomOriginal;
    }

    public void setNomOriginal(String nomOriginal) {
        this.nomOriginal = nomOriginal;
    }

    public String getNomStocke() {
        return nomStocke;
    }

    public void setNomStocke(String nomStocke) {
        this.nomStocke = nomStocke;
    }

    public String getTypeMime() {
        return typeMime;
    }

    public void setTypeMime(String typeMime) {
        this.typeMime = typeMime;
    }

    public long getTaille() {
        return taille;
    }

    public void setTaille(long taille) {
        this.taille = taille;
    }

    public LocalDateTime getDateDepot() {
        return dateDepot;
    }

    public void setDateDepot(LocalDateTime dateDepot) {
        this.dateDepot = dateDepot;
    }

    public Sinistre getSinistre() {
        return sinistre;
    }

    public void setSinistre(Sinistre sinistre) {
        this.sinistre = sinistre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PieceJustificative)) {
            return false;
        }
        PieceJustificative other = (PieceJustificative) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PieceJustificative{id=" + id + ", nomStocke='" + nomStocke + "'}";
    }
}
