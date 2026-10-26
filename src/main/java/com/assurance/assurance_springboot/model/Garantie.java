package com.assurance.assurance_springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Entité {@code garanties}.
 */
@Entity
@Table(name = "garanties")
public class Garantie {

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

    @ManyToMany(mappedBy = "garanties", fetch = FetchType.LAZY)
    private List<Contrat> contrats = new ArrayList<>();

    @ManyToMany(mappedBy = "garantiesDisponibles", fetch = FetchType.LAZY)
    private Set<TypeAssurance> typesAssurance = new LinkedHashSet<>();

    public Garantie() {
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

    public List<Contrat> getContrats() {
        return contrats;
    }

    public void setContrats(List<Contrat> contrats) {
        this.contrats = contrats;
    }

    public Set<TypeAssurance> getTypesAssurance() {
        return typesAssurance;
    }

    public void setTypesAssurance(Set<TypeAssurance> typesAssurance) {
        this.typesAssurance = typesAssurance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Garantie)) {
            return false;
        }
        Garantie other = (Garantie) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Garantie{id=" + id + ", nom='" + nom + "'}";
    }
}
