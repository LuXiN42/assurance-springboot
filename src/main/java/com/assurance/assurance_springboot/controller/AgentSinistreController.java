package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.model.*;
import com.assurance.assurance_springboot.security.UtilisateurPrincipal;
import com.assurance.assurance_springboot.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/agent/sinistres")
public class AgentSinistreController {

    private final SinistreService service;
    private final CommentaireService commentaires;

    public AgentSinistreController(SinistreService s, CommentaireService c) {
        service = s;
        commentaires = c;
    }

    @GetMapping
    public String liste(
            @RequestParam(required = false) StatutSinistre statut,
            Model m
    ) {
        m.addAttribute("sinistres", service.listerParStatut(statut));
        m.addAttribute("statuts", StatutSinistre.values());
        m.addAttribute("statut", statut);

        return "agent/sinistres";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model m) {
        m.addAttribute("sinistre", service.trouverComplet(id));
        m.addAttribute("commentaires", commentaires.listerParSinistre(id));
        m.addAttribute("statuts", StatutSinistre.values());

        return "agent/sinistre-detail";
    }

    @PostMapping("/{id}/statut")
    public String statut(
            @PathVariable Long id,
            @RequestParam StatutSinistre statut,
            @RequestParam(required = false) String montantIndemnisation,
            RedirectAttributes r
    ) {
        try {
            service.changerStatut(
                    id,
                    statut,
                    montantIndemnisation == null || montantIndemnisation.isBlank()
                            ? null
                            : new BigDecimal(montantIndemnisation.replace(',', '.'))
            );

            r.addFlashAttribute("succes", "Statut du sinistre mis à jour.");

        } catch (BusinessException | NumberFormatException e) {
            r.addFlashAttribute(
                    "erreur",
                    e instanceof BusinessException
                            ? e.getMessage()
                            : "Montant invalide."
            );
        }

        return "redirect:/agent/sinistres/" + id;
    }

    @PostMapping("/{id}/commentaires")
    public String commentaire(
            @PathVariable Long id,
            @RequestParam String texte,
            @AuthenticationPrincipal UtilisateurPrincipal principal,
            RedirectAttributes r
    ) {
        try {
            commentaires.ajouter(id, principal.getId(), texte);
            r.addFlashAttribute("succes", "Commentaire ajouté.");

        } catch (BusinessException e) {
            r.addFlashAttribute("erreur", e.getMessage());
        }

        return "redirect:/agent/sinistres/" + id;
    }
}