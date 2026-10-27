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
@RequestMapping("/agent/contrats")
public class AgentContratController {

    private final ContratService service;
    private final GarantieService garanties;

    public AgentContratController(ContratService s, GarantieService g) {
        service = s;
        garanties = g;
    }

    @GetMapping
    public String liste(
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String nomClient,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) StatutContrat statut,
            Model m
    ) {
        m.addAttribute("contrats", service.rechercher(numero, nomClient, typeId, statut));
        m.addAttribute("types", service.types());
        m.addAttribute("statuts", StatutContrat.values());
        m.addAttribute("numero", numero);
        m.addAttribute("nomClient", nomClient);
        m.addAttribute("typeId", typeId);
        m.addAttribute("statut", statut);

        return "agent/contrats";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model m) {
        preparer(m, new Contrat());
        return "agent/contrat-form";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Long id, Model m) {
        preparer(m, service.trouverComplet(id));
        return "agent/contrat-form";
    }

    private void preparer(Model m, Contrat c) {
        m.addAttribute("contrat", c);
        m.addAttribute("clients", service.clients());
        m.addAttribute("types", service.types());
        m.addAttribute("statuts", StatutContrat.values());
    }

    @PostMapping("/enregistrer")
    public String enregistrer(
            @RequestParam(required = false) Long id,
            @RequestParam String numeroContrat,
            @RequestParam String dateDebut,
            @RequestParam(required = false) String dateFin,
            @RequestParam String prime,
            @RequestParam StatutContrat statut,
            @RequestParam Long clientId,
            @RequestParam Long typeAssuranceId,
            Model m,
            RedirectAttributes ra
    ) {
        try {
            service.enregistrer(
                    id,
                    numeroContrat,
                    LocalDate.parse(dateDebut),
                    dateFin == null || dateFin.isBlank()
                            ? null
                            : LocalDate.parse(dateFin),
                    new BigDecimal(prime.replace(',', '.')),
                    statut,
                    clientId,
                    typeAssuranceId
            );

            ra.addFlashAttribute("succes", "Contrat enregistré.");
            return "redirect:/agent/contrats";

        } catch (BusinessException | java.time.format.DateTimeParseException | NumberFormatException e) {
            Contrat c = new Contrat();
            c.setId(id);
            c.setNumeroContrat(numeroContrat);
            c.setStatut(statut);

            preparer(m, c);

            m.addAttribute(
                    "erreur",
                    e instanceof BusinessException
                            ? e.getMessage()
                            : "Date ou prime invalide."
            );
            m.addAttribute("clientId", clientId);
            m.addAttribute("typeAssuranceId", typeAssuranceId);

            return "agent/contrat-form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model m) {
        Contrat c = service.trouverComplet(id);

        m.addAttribute("contrat", c);
        m.addAttribute("garanties", garanties.garantiesDuContrat(id));
        m.addAttribute("toutesGaranties", garanties.lister());

        return "agent/contrat-detail";
    }

    @PostMapping("/{id}/suspendre")
    public String suspendre(@PathVariable Long id, RedirectAttributes r) {
        return action(id, r, () -> service.suspendre(id), "Contrat suspendu.");
    }

    @PostMapping("/{id}/resilier")
    public String resilier(@PathVariable Long id, RedirectAttributes r) {
        return action(id, r, () -> service.resilier(id), "Contrat résilié.");
    }

    @PostMapping("/{id}/reactiver")
    public String reactiver(@PathVariable Long id, RedirectAttributes r) {
        return action(id, r, () -> service.reactiver(id), "Contrat réactivé.");
    }

    @PostMapping("/{id}/renouveler")
    public String renouveler(
            @PathVariable Long id,
            @RequestParam String nouvelleDateFin,
            RedirectAttributes r
    ) {
        try {
            service.renouveler(id, LocalDate.parse(nouvelleDateFin));
            r.addFlashAttribute("succes", "Contrat renouvelé.");

        } catch (Exception e) {
            r.addFlashAttribute(
                    "erreur",
                    e instanceof BusinessException
                            ? e.getMessage()
                            : "Date de renouvellement invalide."
            );
        }

        return "redirect:/agent/contrats/" + id;
    }

    @PostMapping("/{id}/garanties/associer")
    public String associer(
            @PathVariable Long id,
            @RequestParam Long garantieId,
            RedirectAttributes r
    ) {
        try {
            garanties.associer(garantieId, id);
            r.addFlashAttribute("succes", "Garantie associée.");

        } catch (BusinessException e) {
            r.addFlashAttribute("erreur", e.getMessage());
        }

        return "redirect:/agent/contrats/" + id;
    }

    @PostMapping("/{id}/garanties/{garantieId}/dissocier")
    public String dissocier(
            @PathVariable Long id,
            @PathVariable Long garantieId,
            RedirectAttributes r
    ) {
        try {
            garanties.dissocier(garantieId, id);
            r.addFlashAttribute("succes", "Garantie dissociée.");

        } catch (BusinessException e) {
            r.addFlashAttribute("erreur", e.getMessage());
        }

        return "redirect:/agent/contrats/" + id;
    }

    private String action(Long id, RedirectAttributes r, Runnable op, String ok) {
        try {
            op.run();
            r.addFlashAttribute("succes", ok);

        } catch (BusinessException e) {
            r.addFlashAttribute("erreur", e.getMessage());
        }

        return "redirect:/agent/contrats/" + id;
    }
}