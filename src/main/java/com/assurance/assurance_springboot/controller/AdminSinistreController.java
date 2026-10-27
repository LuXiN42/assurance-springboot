package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.model.StatutSinistre;
import com.assurance.assurance_springboot.service.BusinessException;
import com.assurance.assurance_springboot.service.SinistreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sinistres")
public class AdminSinistreController {
    private final SinistreService service;
    public AdminSinistreController(SinistreService service) { this.service=service; }
    @GetMapping public String liste(Model model) { model.addAttribute("sinistres",service.lister()); return "admin/sinistres"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id, Model model) { model.addAttribute("sinistre",service.trouver(id)); model.addAttribute("statuts",StatutSinistre.values()); return "admin/sinistre-detail"; }
    @PostMapping("/{id}/statut") public String changerStatut(@PathVariable Long id, @RequestParam StatutSinistre statut, @RequestParam(required=false) String montantIndemnisation, RedirectAttributes ra) { try { java.math.BigDecimal montant=montantIndemnisation==null||montantIndemnisation.isBlank()?null:new java.math.BigDecimal(montantIndemnisation.replace(',','.')); service.changerStatut(id,statut,montant); ra.addFlashAttribute("succes","Statut du sinistre mis à jour."); } catch(BusinessException | NumberFormatException e) { ra.addFlashAttribute("erreur",e instanceof BusinessException?e.getMessage():"Montant d'indemnisation invalide."); } return "redirect:/admin/sinistres/"+id; }
    @PostMapping("/{id}/supprimer") public String supprimer(@PathVariable Long id, RedirectAttributes ra) { try { service.supprimer(id); ra.addFlashAttribute("succes","Sinistre supprimé."); } catch(BusinessException e) { ra.addFlashAttribute("erreur",e.getMessage()); } return "redirect:/admin/sinistres"; }
}
