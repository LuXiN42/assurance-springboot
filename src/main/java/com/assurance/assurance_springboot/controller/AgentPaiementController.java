package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.model.*;
import com.assurance.assurance_springboot.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/agent/paiements")
public class AgentPaiementController {

    private final PaiementService service;
    private final ContratService contrats;

    public AgentPaiementController(PaiementService s, ContratService c) {
        service = s;
        contrats = c;
    }

    @GetMapping("/en-attente")
    public String enAttente(Model m) {
        m.addAttribute("paiements", service.enAttente());
        m.addAttribute("statuts", StatutPaiement.values());
        m.addAttribute("modes", ModePaiement.values());

        return "agent/paiements";
    }

    @GetMapping("/echeances")
    public String echeances(@RequestParam(defaultValue = "30") int jours, Model m) {
        m.addAttribute("paiements", service.echeances(jours));
        m.addAttribute("statuts", StatutPaiement.values());
        m.addAttribute("modes", ModePaiement.values());
        m.addAttribute("jours", jours);

        return "agent/paiements";
    }

    @GetMapping("/client/{clientId}")
    public String historiqueClient(@PathVariable Long clientId, Model m) {
        m.addAttribute("paiements", service.historiqueClient(clientId));
        m.addAttribute("statuts", StatutPaiement.values());
        m.addAttribute("modes", ModePaiement.values());
        m.addAttribute("clientId", clientId);

        return "agent/paiements";
    }

    @GetMapping("/contrat/{contratId}")
    public String parContrat(@PathVariable Long contratId, Model m) {
        m.addAttribute("paiements", service.parContrat(contratId));
        m.addAttribute("statuts", StatutPaiement.values());
        m.addAttribute("modes", ModePaiement.values());
        m.addAttribute("contratId", contratId);

        return "agent/paiements";
    }

    @GetMapping
    public String liste(
            @RequestParam(required = false) StatutPaiement statut,
            @RequestParam(required = false) Integer jours,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long contratId,
            Model m
    ) {
        if (clientId != null) {
            m.addAttribute("paiements", service.historiqueClient(clientId));
        } else if (contratId != null) {
            m.addAttribute("paiements", service.parContrat(contratId));
        } else if (jours != null) {
            m.addAttribute("paiements", service.echeances(jours));
        } else if (statut != null) {
            m.addAttribute("paiements", service.parStatut(statut));
        } else {
            m.addAttribute("paiements", service.tous());
        }

        m.addAttribute("statuts", StatutPaiement.values());
        m.addAttribute("modes", ModePaiement.values());
        m.addAttribute("jours", jours);
        m.addAttribute("clientId", clientId);
        m.addAttribute("contratId", contratId);

        return "agent/paiements";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model m) {
        preparer(m);
        return "agent/paiement-form";
    }

    private void preparer(Model m) {
        m.addAttribute("contrats", contrats.lister());
        m.addAttribute("modes", ModePaiement.values());
        m.addAttribute("statuts", StatutPaiement.values());
        m.addAttribute("modes", ModePaiement.values());
    }

    @PostMapping("/enregistrer")
    public String enregistrer(
            @RequestParam Long contratId,
            @RequestParam String montant,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) ModePaiement mode,
            @RequestParam(required = false) StatutPaiement statut,
            Model m,
            RedirectAttributes r
    ) {
        try {
            service.enregistrer(
                    contratId,
                    new BigDecimal(montant.replace(',', '.')),
                    date == null || date.isBlank() ? null : LocalDate.parse(date),
                    mode,
                    statut
            );

            r.addFlashAttribute("succes", "Paiement enregistré.");
            return "redirect:/agent/paiements";

        } catch (Exception e) {
            preparer(m);
            m.addAttribute(
                    "erreur",
                    e instanceof BusinessException
                            ? e.getMessage()
                            : "Date ou montant invalide."
            );

            return "agent/paiement-form";
        }
    }

    @PostMapping("/{id}/valider")
    public String valider(
            @PathVariable Long id,
            @RequestParam(required = false) ModePaiement mode,
            RedirectAttributes r
    ) {
        try {
            service.valider(id, mode);
            r.addFlashAttribute("succes", "Paiement validé.");

        } catch (BusinessException e) {
            r.addFlashAttribute("erreur", e.getMessage());
        }

        return "redirect:/agent/paiements";
    }

    @PostMapping("/{id}/echouer")
    public String echouer(@PathVariable Long id, RedirectAttributes r) {
        return transition(id, r, true);
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id, RedirectAttributes r) {
        return transition(id, r, false);
    }

    private String transition(Long id, RedirectAttributes r, boolean fail) {
        try {
            if (fail) {
                service.echouer(id);
            } else {
                service.annuler(id);
            }

            r.addFlashAttribute("succes", "Statut du paiement mis à jour.");

        } catch (BusinessException e) {
            r.addFlashAttribute("erreur", e.getMessage());
        }

        return "redirect:/agent/paiements";
    }
}