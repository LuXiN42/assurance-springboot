package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.service.AssistantIAService;
import com.assurance.assurance_springboot.service.BusinessException;
import com.assurance.assurance_springboot.service.StatistiquesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/statistiques")
public class AdminStatistiquesController {
    private final StatistiquesService service;
    private final AssistantIAService assistant;
    public AdminStatistiquesController(StatistiquesService service, AssistantIAService assistant) { this.service=service; this.assistant=assistant; }
    @GetMapping public String afficher(Model model) { model.addAllAttributes(service.globales()); return "admin/statistiques"; }

    @PostMapping("/analyse")
    public String analyser(RedirectAttributes redirect) {
        try {
            redirect.addFlashAttribute("analyseIA", assistant.analyserStatistiques(service.globales()));
        } catch (BusinessException e) {
            redirect.addFlashAttribute("erreurIA", e.getMessage());
        }
        return "redirect:/admin/statistiques";
    }
}
