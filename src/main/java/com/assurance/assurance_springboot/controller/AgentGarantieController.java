package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.model.Garantie;
import com.assurance.assurance_springboot.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/agent/garanties")
public class AgentGarantieController {

    private final GarantieService service;

    public AgentGarantieController(GarantieService s) {
        service = s;
    }

    @GetMapping
    public String liste(Model m) {
        m.addAttribute("garanties", service.lister());
        return "agent/garanties";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model m) {
        m.addAttribute("garantie", new Garantie());
        return "agent/garantie-form";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Long id, Model m) {
        m.addAttribute("garantie", service.trouver(id));
        return "agent/garantie-form";
    }

    @PostMapping("/enregistrer")
    public String enregistrer(
            @RequestParam(required = false) Long id,
            @RequestParam String nom,
            @RequestParam(required = false) String description,
            Model m,
            RedirectAttributes r
    ) {
        try {
            if (id == null) {
                service.creer(nom, description);
            } else {
                service.modifier(id, nom, description);
            }

            r.addFlashAttribute("succes", "Garantie enregistrée.");
            return "redirect:/agent/garanties";

        } catch (BusinessException e) {
            Garantie g = new Garantie();
            g.setId(id);
            g.setNom(nom);
            g.setDescription(description);

            m.addAttribute("garantie", g);
            m.addAttribute("erreur", e.getMessage());

            return "agent/garantie-form";
        }
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes r) {
        try {
            service.supprimer(id);
            r.addFlashAttribute("succes", "Garantie supprimée.");

        } catch (BusinessException e) {
            r.addFlashAttribute("erreur", e.getMessage());
        }

        return "redirect:/agent/garanties";
    }
}