package com.assurance.assurance_springboot.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité {@code contrats}.
 */
@Entity
@Table(name = "contrats",
        uniqueConstraints = @UniqueConstraint(name = "uk_contrat_numero", columnNames = "numero_contrat"))
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 80)
    @Column(name = "numero_contrat", nullable = false, length = 80)
    private String numeroContrat;

    @NotNull
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @NotNull
    @Positive
    @Column(name = "prime", nullable = false, precision = 12, scale = 2)
    private BigDecimal prime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "statut", nullable = false, length = 32)
    private StatutContrat statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_assurance_id", nullable = false)
    private TypeAssurance typeAssurance;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "contrat_garanties",
            joinColumns = @JoinColumn(name = "contrat_id"),
            inverseJoinColumns = @JoinColumn(name = "garantie_id"))
    private List<Garantie> garanties = new ArrayList<>();

    @OneToMany(mappedBy = "contrat", fetch = FetchType.LAZY)
    private List<Sinistre> sinistres = new ArrayList<>();

    @OneToMany(mappedBy = "contrat", fetch = FetchType.LAZY)
    private List<Paiement> paiements = new ArrayList<>();

    public Contrat() {
    }

    /**
     * Équivalent applicatif de la contrainte CHECK {@code chk_contrat_dates}
     * ({@code date_fin IS NULL OR date_fin > date_debut}).
     */
    @AssertTrue(message = "date_fin doit être strictement postérieure à date_debut")
    public boolean isDateFinCoherente() {
        return dateFin == null || dateDebut == null || dateFin.isAfter(dateDebut);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroContrat() {
        return numeroContrat;
    }

    public void setNumeroContrat(String numeroContrat) {
        this.numeroContrat = numeroContrat;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public BigDecimal getPrime() {
        return prime;
    }

    public void setPrime(BigDecimal prime) {
        this.prime = prime;
    }

    public StatutContrat getStatut() {
        return statut;
    }

    public void setStatut(StatutContrat statut) {
        this.statut = statut;
    }

    public Utilisateur getClient() {
        return client;
    }

    public void setClient(Utilisateur client) {
        this.client = client;
    }

    public TypeAssurance getTypeAssurance() {
        return typeAssurance;
    }

    public void setTypeAssurance(TypeAssurance typeAssurance) {
        this.typeAssurance = typeAssurance;
    }

    public List<Garantie> getGaranties() {
        return garanties;
    }

    public void setGaranties(List<Garantie> garanties) {
        this.garanties = garanties;
    }

    public List<Sinistre> getSinistres() {
        return sinistres;
    }

    public void setSinistres(List<Sinistre> sinistres) {
        this.sinistres = sinistres;
    }

    public List<Paiement> getPaiements() {
        return paiements;
    }

    public void setPaiements(List<Paiement> paiements) {
        this.paiements = paiements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Contrat)) {
            return false;
        }
        Contrat other = (Contrat) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Contrat{id=" + id + ", numeroContrat='" + numeroContrat + "', statut=" + statut + '}';
    }
}
