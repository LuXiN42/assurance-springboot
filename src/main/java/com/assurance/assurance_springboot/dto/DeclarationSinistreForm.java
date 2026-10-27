package com.assurance.assurance_springboot.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Données saisies par le client pour déclarer un sinistre. */
public class DeclarationSinistreForm {
    private Long contratId;
    private LocalDate date;
    private String description;
    private BigDecimal montantEstime;
    private String typeSinistre;

    public Long getContratId() { return contratId; }
    public void setContratId(Long contratId) { this.contratId = contratId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getMontantEstime() { return montantEstime; }
    public void setMontantEstime(BigDecimal montantEstime) { this.montantEstime = montantEstime; }
    public String getTypeSinistre() { return typeSinistre; }
    public void setTypeSinistre(String typeSinistre) { this.typeSinistre = typeSinistre; }
}
