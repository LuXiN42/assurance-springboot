package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.model.Role;
import com.assurance.assurance_springboot.repository.*;
import com.assurance.assurance_springboot.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/agent/clients")
public class AgentClientController {

    private final UtilisateurRepository clients;
    private final ContratRepository contrats;
    private final SinistreRepository sinistres;
    private final PaiementService paiements;

    public AgentClientController(
            UtilisateurRepository u,
            ContratRepository c,
            SinistreRepository s,
            PaiementService p
    ) {
        clients = u;
        contrats = c;
        sinistres = s;
        paiements = p;
    }

    @GetMapping
    public String liste(Model m) {
        m.addAttribute(
                "clients",
                clients.findByRoleOrderByNomAscPrenomAsc(Role.CLIENT)
        );
        return "agent/clients";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model m) {
        var client = clients.findById(id)
                .filter(u -> u.getRole() == Role.CLIENT)
                .orElseThrow(() -> new BusinessException("Client introuvable."));

        m.addAttribute("client", client);
        m.addAttribute("contrats", contrats.findByClientIdOrderByDateDebutDesc(id));
        m.addAttribute("sinistres", sinistres.findByContratClientIdOrderByDateDesc(id));
        m.addAttribute("paiements", paiements.historiqueClient(id));

        return "agent/client-detail";
    }
}