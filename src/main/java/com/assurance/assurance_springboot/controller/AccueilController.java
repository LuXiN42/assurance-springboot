package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.security.UtilisateurPrincipal;
import com.assurance.assurance_springboot.service.StatistiquesService;
import com.assurance.assurance_springboot.service.PaiementService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccueilController {

    private final StatistiquesService statistiques;
    private final PaiementService paiements;
    public AccueilController(StatistiquesService statistiques, PaiementService paiements) { this.statistiques = statistiques; this.paiements = paiements; }

    @GetMapping("/")
    public String racine() {
        return "redirect:/login";
    }

    @GetMapping("/client/accueil")
    public String accueilClient(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("prenom", principal.getPrenom());
        return "client/accueil";
    }

    @GetMapping("/agent/accueil")
    public String accueilAgent(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("prenom", principal.getPrenom());
        return "agent/accueil";
    }

    @GetMapping("/admin/accueil")
    public String accueilAdmin(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("prenom", principal.getPrenom());
        return "admin/accueil";
    }
}