package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.security.UtilisateurPrincipal;
import com.assurance.assurance_springboot.service.BusinessException;
import com.assurance.assurance_springboot.service.ClientEspaceService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Profil commun aux trois rôles : on y arrive en cliquant sur son prénom dans la barre du haut. */
@Controller
@RequestMapping("/profil")
public class ProfilController {
    private final ClientEspaceService service;
    public ProfilController(ClientEspaceService service) { this.service = service; }

    @GetMapping
    public String profil(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("utilisateur", service.profil(principal.getId()));
        return "client/profil";
    }

    @PostMapping
    public String modifierProfil(@RequestParam String nom, @RequestParam String prenom, @RequestParam String email,
            @RequestParam(required=false) String telephone, @RequestParam(required=false) String adresse,
            @AuthenticationPrincipal UtilisateurPrincipal principal, RedirectAttributes redirect) {
        try {
            service.modifierProfil(principal.getId(), nom, prenom, email, telephone, adresse);
            redirect.addFlashAttribute("succes", "Vos informations ont été mises à jour.");
        } catch (BusinessException e) { redirect.addFlashAttribute("erreur", e.getMessage()); }
        return "redirect:/profil";
    }

    @PostMapping("/mot-de-passe")
    public String changerMotDePasse(@RequestParam String motDePasseActuel,
            @RequestParam String nouveauMotDePasse, @RequestParam String confirmationMotDePasse,
            @AuthenticationPrincipal UtilisateurPrincipal principal, RedirectAttributes redirect) {
        try {
            service.changerMotDePasse(principal.getId(), motDePasseActuel, nouveauMotDePasse, confirmationMotDePasse);
            redirect.addFlashAttribute("succes", "Votre mot de passe a été changé.");
        } catch (BusinessException e) { redirect.addFlashAttribute("erreur", e.getMessage()); }
        return "redirect:/profil";
    }
}
