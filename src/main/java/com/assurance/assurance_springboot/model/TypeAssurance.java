package com.assurance.assurance_springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "types_assurance")
public class TypeAssurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @PositiveOrZero
    @Column(name = "prix_base", precision = 12, scale = 2)
    private BigDecimal prixBase;

    @OneToMany(mappedBy = "typeAssurance", fetch = FetchType.LAZY)
    private Set<Contrat> contrats = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "type_assurance_garanties",
        joinColumns = @JoinColumn(name = "type_assurance_id"),
        inverseJoinColumns = @JoinColumn(name = "garantie_id"))
    private Set<Garantie> garantiesDisponibles = new HashSet<>();

    public TypeAssurance() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrixBase() {
        return prixBase;
    }

    public void setPrixBase(BigDecimal prixBase) {
        this.prixBase = prixBase;
    }

    public Set<Contrat> getContrats() {
        return contrats;
    }

    public void setContrats(Set<Contrat> contrats) {
        this.contrats = contrats;
    }

    public Set<Garantie> getGarantiesDisponibles() {
        return garantiesDisponibles;
    }

    public void setGarantiesDisponibles(Set<Garantie> garantiesDisponibles) {
        this.garantiesDisponibles = garantiesDisponibles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypeAssurance)) {
            return false;
        }
        TypeAssurance other = (TypeAssurance) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "TypeAssurance{id=" + id + ", nom='" + nom + "'}";
    }
}
