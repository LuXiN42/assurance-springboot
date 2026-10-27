package com.assurance.assurance_springboot.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.assurance.assurance_springboot.service.UtilisateurService;
import com.assurance.assurance_springboot.model.Utilisateur;
import com.assurance.assurance_springboot.model.Role;
import com.assurance.assurance_springboot.service.BusinessException;
import com.assurance.assurance_springboot.security.UtilisateurPrincipal;


@Controller
@RequestMapping("/admin/utilisateurs")
public class AdminUtilisateurController {

    private final UtilisateurService service;

    public AdminUtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("utilisateurs", service.lister());
        return "admin/utilisateurs";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        Utilisateur u = new Utilisateur();
        u.setRole(Role.CLIENT);
        model.addAttribute("utilisateur", u);
        model.addAttribute("roles", Role.values());
        return "admin/utilisateur-form";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Long id, Model model) {
        model.addAttribute("utilisateur", service.trouver(id));
        model.addAttribute("roles", Role.values());
        return "admin/utilisateur-form";
    }

    @PostMapping("/enregistrer")
    public String enregistrer(@RequestParam(required = false) Long id,
                              @RequestParam String nom,
                              @RequestParam String prenom,
                              @RequestParam String email,
                              @RequestParam(required = false) String telephone,
                              @RequestParam(required = false) String adresse,
                              @RequestParam(required = false) String motDePasse,
                              @RequestParam Role role,
                              Model model, RedirectAttributes ra) {
        try {
            service.enregistrer(id, nom, prenom, email, telephone, adresse, motDePasse, role);

            // Si l'admin vient de modifier son propre compte, on rafraîchit la session
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (id != null && auth != null
                    && auth.getPrincipal() instanceof UtilisateurPrincipal p
                    && id.equals(p.getUtilisateur().getId())) {
                UtilisateurPrincipal nouveau = new UtilisateurPrincipal(service.trouver(id));
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(nouveau, auth.getCredentials(), nouveau.getAuthorities()));
            }

            ra.addFlashAttribute("succes", "Utilisateur enregistré.");
            return "redirect:/admin/utilisateurs";
        } catch (BusinessException e) {
            Utilisateur u = new Utilisateur();
            u.setId(id);
            u.setNom(nom);
            u.setPrenom(prenom);
            u.setEmail(email);
            u.setTelephone(telephone);
            u.setAdresse(adresse);
            u.setRole(role);
            model.addAttribute("utilisateur", u);
            model.addAttribute("roles", Role.values());
            model.addAttribute("erreur", e.getMessage());
            return "admin/utilisateur-form";
        }
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            service.supprimer(id, principal.getName());
            ra.addFlashAttribute("succes", "Utilisateur supprimé.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }
}