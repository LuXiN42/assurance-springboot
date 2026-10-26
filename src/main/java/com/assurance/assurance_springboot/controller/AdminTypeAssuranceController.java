package com.assurance.assurance_springboot.controller;

import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.assurance.assurance_springboot.service.TypeAssuranceService;
import com.assurance.assurance_springboot.model.TypeAssurance;
import com.assurance.assurance_springboot.service.BusinessException;


@Controller
@RequestMapping("/admin/types")
public class AdminTypeAssuranceController {

    private final TypeAssuranceService service;

    public AdminTypeAssuranceController(TypeAssuranceService service) {
        this.service = service;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("types", service.lister());
        return "admin/types";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("type", new TypeAssurance());
        return "admin/type-form";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Long id, Model model) {
        model.addAttribute("type", service.trouver(id));
        return "admin/type-form";
    }

    @PostMapping("/enregistrer")
    public String enregistrer(@RequestParam(required = false) Long id,
                              @RequestParam String nom,
                              @RequestParam(required = false) String description,
                              @RequestParam String prixBase,
                              Model model,
                              RedirectAttributes ra) {
        try {
            BigDecimal prix = new BigDecimal(prixBase.replace(',', '.'));
            service.enregistrer(id, nom, description, prix);
            ra.addFlashAttribute("succes", "Type enregistré.");
            return "redirect:/admin/types";
        } catch (NumberFormatException | BusinessException e) {
            TypeAssurance t = new TypeAssurance();
            t.setId(id);
            t.setNom(nom);
            t.setDescription(description);
            model.addAttribute("type", t);
            model.addAttribute("prixSaisi", prixBase);
            model.addAttribute("erreur", e instanceof BusinessException ? e.getMessage() : "Prix invalide.");
            return "admin/type-form";
        }
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.supprimer(id);
            ra.addFlashAttribute("succes", "Type supprimé.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin/types";
    }
}