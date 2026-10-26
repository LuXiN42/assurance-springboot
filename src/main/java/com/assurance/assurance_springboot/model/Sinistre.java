package com.assurance.assurance_springboot.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité {@code sinistres}.
 */
@Entity
@Table(name = "sinistres",
        uniqueConstraints = @UniqueConstraint(name = "uk_sinistre_numero", columnNames = "numero_sinistre"))
public class Sinistre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 1, max = 80)
    @Column(name = "numero_sinistre", nullable = false, length = 80)
    private String numeroSinistre;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @Size(min = 1, max = 2000)
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @PositiveOrZero
    @Column(name = "montant_estime", precision = 12, scale = 2)
    private BigDecimal montantEstime;

    @Size(max = 120)
    @Column(name = "type_sinistre", length = 120)
    private String typeSinistre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "statut", nullable = false, length = 32)
    private StatutSinistre statut;

    @PositiveOrZero
    @Column(name = "montant_indemnisation", precision = 12, scale = 2)
    private BigDecimal montantIndemnisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrat_id", nullable = false)
    private Contrat contrat;

    @OneToMany(mappedBy = "sinistre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PieceJustificative> pieces = new ArrayList<>();

    @OneToMany(mappedBy = "sinistre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("dateCreation ASC")
    private List<Commentaire> commentaires = new ArrayList<>();

    public Sinistre() {
    }

    /** Rappel métier conservé depuis l'entité JEE : le client est porté par le contrat. */
    @Transient
    public Utilisateur getClient() {
        return contrat != null ? contrat.getClient() : null;
    }

    /**
     * Équivalent applicatif de la contrainte CHECK
     * {@code chk_sinistre_indemn_statut} : l'indemnisation n'est renseignée que
     * pour un sinistre ACCEPTÉ ou INDEMNISÉ, et elle est obligatoire si le
     * statut est INDEMNISÉ.
     */
    @AssertTrue(message = "montant_indemnisation : cohérent uniquement avec un statut ACCEPTE ou INDEMNISE, et obligatoire si INDEMNISE")
    public boolean isIndemnisationCoherente() {
        if (montantIndemnisation == null) {
            return statut != StatutSinistre.INDEMNISE;
        }
        return statut == StatutSinistre.ACCEPTE || statut == StatutSinistre.INDEMNISE;
    }

    public void ajouterPiece(PieceJustificative p) {
        pieces.add(p);
        p.setSinistre(this);
    }

    public void ajouterCommentaire(Commentaire c) {
        c.setSinistre(this);
        this.commentaires.add(c);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroSinistre() {
        return numeroSinistre;
    }

    public void setNumeroSinistre(String numeroSinistre) {
        this.numeroSinistre = numeroSinistre;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getMontantEstime() {
        return montantEstime;
    }

    public void setMontantEstime(BigDecimal montantEstime) {
        this.montantEstime = montantEstime;
    }

    public String getTypeSinistre() {
        return typeSinistre;
    }

    public void setTypeSinistre(String typeSinistre) {
        this.typeSinistre = typeSinistre;
    }

    public StatutSinistre getStatut() {
        return statut;
    }

    public void setStatut(StatutSinistre statut) {
        this.statut = statut;
    }

    public BigDecimal getMontantIndemnisation() {
        return montantIndemnisation;
    }

    public void setMontantIndemnisation(BigDecimal montantIndemnisation) {
        this.montantIndemnisation = montantIndemnisation;
    }

    public Contrat getContrat() {
        return contrat;
    }

    public void setContrat(Contrat contrat) {
        this.contrat = contrat;
    }

    public List<PieceJustificative> getPieces() {
        return pieces;
    }

    public void setPieces(List<PieceJustificative> pieces) {
        this.pieces = pieces;
    }

    public List<Commentaire> getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(List<Commentaire> commentaires) {
        this.commentaires = commentaires;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sinistre)) {
            return false;
        }
        Sinistre other = (Sinistre) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Sinistre{id=" + id + ", numeroSinistre='" + numeroSinistre + "', statut=" + statut + '}';
    }
}
