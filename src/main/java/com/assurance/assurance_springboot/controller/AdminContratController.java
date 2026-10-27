package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.StatutContrat;
import com.assurance.assurance_springboot.service.BusinessException;
import com.assurance.assurance_springboot.service.ContratService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/contrats")
public class AdminContratController {
    private final ContratService service;
    public AdminContratController(ContratService service) { this.service=service; }
    @GetMapping public String liste(Model model) { model.addAttribute("contrats",service.lister()); return "admin/contrats"; }
    @GetMapping("/nouveau") public String nouveau(Model model) { preparer(model,new Contrat()); return "admin/contrat-form"; }
    @GetMapping("/{id}/modifier") public String modifier(@PathVariable Long id, Model model) { preparer(model,service.trouver(id)); return "admin/contrat-form"; }
    private void preparer(Model model, Contrat c) { model.addAttribute("contrat",c); model.addAttribute("clients",service.clients()); model.addAttribute("types",service.types()); model.addAttribute("statuts",StatutContrat.values()); }
    @PostMapping("/enregistrer") public String enregistrer(@RequestParam(required=false) Long id, @RequestParam String numeroContrat,
        @RequestParam String dateDebut, @RequestParam(required=false) String dateFin, @RequestParam String prime,
        @RequestParam StatutContrat statut, @RequestParam Long clientId, @RequestParam Long typeAssuranceId, Model model, RedirectAttributes ra) {
        try { LocalDate debut=LocalDate.parse(dateDebut); LocalDate fin=dateFin==null||dateFin.isBlank()?null:LocalDate.parse(dateFin); BigDecimal p=new BigDecimal(prime.replace(',','.'));
            service.enregistrer(id,numeroContrat,debut,fin,p,statut,clientId,typeAssuranceId); ra.addFlashAttribute("succes","Contrat enregistré."); return "redirect:/admin/contrats";
        } catch (BusinessException | java.time.format.DateTimeParseException | NumberFormatException e) {
            Contrat c=new Contrat(); c.setId(id); c.setNumeroContrat(numeroContrat); c.setStatut(statut);
            try { c.setDateDebut(LocalDate.parse(dateDebut)); } catch(Exception ignored) {} try { if(dateFin!=null&&!dateFin.isBlank()) c.setDateFin(LocalDate.parse(dateFin)); } catch(Exception ignored) {}
            try { c.setPrime(new BigDecimal(prime.replace(',','.'))); } catch(Exception ignored) {}
            preparer(model,c); model.addAttribute("clientId",clientId); model.addAttribute("typeAssuranceId",typeAssuranceId); model.addAttribute("erreur", e instanceof BusinessException?e.getMessage():"Date ou prime invalide."); return "admin/contrat-form";
        }
    }
    @PostMapping("/{id}/supprimer") public String supprimer(@PathVariable Long id, RedirectAttributes ra) { try { service.supprimer(id); ra.addFlashAttribute("succes","Contrat supprimé."); } catch(BusinessException e) { ra.addFlashAttribute("erreur",e.getMessage()); } return "redirect:/admin/contrats"; }
}
