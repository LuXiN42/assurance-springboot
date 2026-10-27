package com.assurance.assurance_springboot.service;

import com.assurance.assurance_springboot.model.Role;
import com.assurance.assurance_springboot.model.StatutContrat;
import com.assurance.assurance_springboot.model.StatutPaiement;
import com.assurance.assurance_springboot.model.StatutSinistre;
import com.assurance.assurance_springboot.repository.ContratRepository;
import com.assurance.assurance_springboot.repository.PaiementRepository;
import com.assurance.assurance_springboot.repository.SinistreRepository;
import com.assurance.assurance_springboot.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatistiquesService {

    private final UtilisateurRepository utilisateurs;
    private final ContratRepository contrats;
    private final SinistreRepository sinistres;
    private final PaiementRepository paiements;

    public StatistiquesService(
            UtilisateurRepository utilisateurs,
            ContratRepository contrats,
            SinistreRepository sinistres,
            PaiementRepository paiements) {
        this.utilisateurs = utilisateurs;
        this.contrats = contrats;
        this.sinistres = sinistres;
        this.paiements = paiements;
    }

    public Map<String, Object> globales() {
        Map<String, Object> statistiques = new LinkedHashMap<>();

        LocalDate aujourdHui = LocalDate.now();
        LocalDate debutPeriodeNouveauxClients = aujourdHui.minusDays(29);

        // Répartition des utilisateurs par rôle.
        Map<Role, Long> utilisateursParRole = new LinkedHashMap<>();

        for (Role role : Role.values()) {
            utilisateursParRole.put(role, utilisateurs.countByRole(role));
        }

        // Répartition des contrats par statut.
        Map<StatutContrat, Long> contratsParStatut = new LinkedHashMap<>();

        for (StatutContrat statut : StatutContrat.values()) {
            contratsParStatut.put(statut, contrats.countByStatut(statut));
        }

        // Répartition des sinistres par statut.
        Map<StatutSinistre, Long> sinistresParStatut = new LinkedHashMap<>();

        for (StatutSinistre statut : StatutSinistre.values()) {
            sinistresParStatut.put(statut, sinistres.countByStatut(statut));
        }

        // Nombre de contrats par type d'assurance.
        List<Map<String, Object>> contratsParType = new ArrayList<>();

        for (Object[] ligne : contrats.compterParType()) {
            Map<String, Object> element = new LinkedHashMap<>();
            element.put("id", ligne[0]);
            element.put("nom", ligne[1]);
            element.put("nombre", ligne[2]);
            contratsParType.add(element);
        }

        // Nombre de clients distincts par type d'assurance.
        List<Map<String, Object>> clientsParType = new ArrayList<>();

        for (Object[] ligne : contrats.compterClientsDistinctsParType()) {
            Map<String, Object> element = new LinkedHashMap<>();
            element.put("id", ligne[0]);
            element.put("nom", ligne[1]);
            element.put("nombre", ligne[2]);
            clientsParType.add(element);
        }

        statistiques.put("utilisateursParRole", utilisateursParRole);
        statistiques.put("contratsParStatut", contratsParStatut);
        statistiques.put("sinistresParStatut", sinistresParStatut);
        statistiques.put("contratsParType", contratsParType);
        statistiques.put("clientsParType", clientsParType);

        // Indicateurs clients.
        statistiques.put(
                "nombreClients",
                utilisateurs.countByRole(Role.CLIENT)
        );

        statistiques.put(
                "nouveauxClients",
                utilisateurs.countByRoleAndDateInscriptionBetween(
                        Role.CLIENT,
                        debutPeriodeNouveauxClients,
                        aujourdHui
                )
        );

        statistiques.put(
                "debutPeriodeNouveauxClients",
                debutPeriodeNouveauxClients
        );

        statistiques.put("finPeriodeNouveauxClients", aujourdHui);

        // Indicateurs contrats et sinistres.
        statistiques.put("nombreContrats", contrats.count());
        statistiques.put("nombreSinistres", sinistres.count());

        // Indicateurs financiers.
        statistiques.put(
                "totalPrimes",
                safe(contrats.sommePrimes())
        );

        statistiques.put(
                "totalPaiementsEffectues",
                safe(paiements.sommeParStatut(StatutPaiement.EFFECTUE))
        );

        statistiques.put(
                "totalMontantsEstimes",
                safe(sinistres.sommeMontantsEstimes())
        );

        statistiques.put(
                "totalIndemnisations",
                safe(sinistres.sommeIndemnisations())
        );

        // Évolution mensuelle sur les six derniers mois, mois courant inclus.
        statistiques.put(
                "contratsParMois",
                construireEvolutionContrats()
        );

        statistiques.put(
                "paiementsParMois",
                construireEvolutionPaiements()
        );

        return statistiques;
    }

    /**
     * Construit l'évolution mensuelle du nombre de contrats créés.
     */
    private List<Map<String, Object>> construireEvolutionContrats() {
        List<Map<String, Object>> evolution = new ArrayList<>();

        YearMonth moisCourant = YearMonth.now();
        DateTimeFormatter formateur =
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);

        for (int decalage = 5; decalage >= 0; decalage--) {
            YearMonth mois = moisCourant.minusMonths(decalage);

            LocalDate debut = mois.atDay(1);
            LocalDate fin = mois.atEndOfMonth();

            Map<String, Object> element = new LinkedHashMap<>();
            element.put("mois", formaterMois(mois, formateur));
            element.put("nombre", contrats.countByDateDebutBetween(debut, fin));

            evolution.add(element);
        }

        return evolution;
    }

    /**
     * Construit l'évolution mensuelle des paiements effectivement enregistrés.
     */
    private List<Map<String, Object>> construireEvolutionPaiements() {
        List<Map<String, Object>> evolution = new ArrayList<>();

        YearMonth moisCourant = YearMonth.now();
        DateTimeFormatter formateur =
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);

        for (int decalage = 5; decalage >= 0; decalage--) {
            YearMonth mois = moisCourant.minusMonths(decalage);

            LocalDate debut = mois.atDay(1);
            LocalDate fin = mois.atEndOfMonth();

            Map<String, Object> element = new LinkedHashMap<>();
            element.put("mois", formaterMois(mois, formateur));

            element.put(
                    "montant",
                    safe(paiements.sommeParStatutEtPeriode(
                            StatutPaiement.EFFECTUE,
                            debut,
                            fin
                    ))
            );

            evolution.add(element);
        }

        return evolution;
    }

    private String formaterMois(
            YearMonth mois,
            DateTimeFormatter formateur) {
        String resultat = mois.atDay(1).format(formateur);
        return resultat.substring(0, 1).toUpperCase(Locale.FRENCH)
                + resultat.substring(1);
    }

    private BigDecimal safe(BigDecimal montant) {
        return montant == null ? BigDecimal.ZERO : montant;
    }
}