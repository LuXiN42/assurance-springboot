package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.service.StatistiquesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/statistiques")
public class AdminStatistiquesController {
    private final StatistiquesService service;
    public AdminStatistiquesController(StatistiquesService service) { this.service=service; }
    @GetMapping public String afficher(Model model) { model.addAllAttributes(service.globales()); return "admin/statistiques"; }
}
