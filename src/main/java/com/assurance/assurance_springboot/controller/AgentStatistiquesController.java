package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.service.StatistiquesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/agent/statistiques")
public class AgentStatistiquesController {

    private final StatistiquesService service;

    public AgentStatistiquesController(StatistiquesService s) {
        service = s;
    }

    @GetMapping
    public String afficher(Model m) {
        m.addAllAttributes(service.globales());
        return "agent/statistiques";
    }
}