package com.assurance.assurance_springboot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.assurance.assurance_springboot.security.UtilisateurPrincipal;
import com.assurance.assurance_springboot.service.AssistantIAService;
import com.assurance.assurance_springboot.service.BusinessException;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/client/assistant")
public class AssistantController {

    private static final String HISTORIQUE = "historiqueIA";
    // on garde seulement les 20 derniers messages pour ne pas envoyer trop de texte
    private static final int MAX_MESSAGES = 20;

    private final AssistantIAService assistant;

    public AssistantController(AssistantIAService assistant) {
        this.assistant = assistant;
    }

    @GetMapping
    public String page() {
        return "client/assistant";
    }

    // la bulle et la page Bibi récupèrent la conversation en JSON
    @GetMapping(params = "format=json")
    @ResponseBody
    public Map<String, Object> conversation(HttpSession session) {
        List<Map<String, String>> messages = new ArrayList<>();
        for (String[] m : historique(session)) {
            messages.add(Map.of("role", m[0], "texte", m[1]));
        }
        return Map.of("messages", messages);
    }

    @PostMapping(params = "format=json")
    @ResponseBody
    public Map<String, Object> envoyer(@RequestParam(required = false) String question,
            @RequestParam(required = false) String action,
            @AuthenticationPrincipal UtilisateurPrincipal principal, HttpSession session) {

        Map<String, Object> resultat = new HashMap<>();

        if ("effacer".equals(action)) {
            session.removeAttribute(HISTORIQUE);
            return resultat;
        }

        String texte = question == null ? "" : question.trim();
        if (texte.length() < 2 || texte.length() > 500) {
            resultat.put("erreur", "La question doit faire entre 2 et 500 caractères.");
            return resultat;
        }

        List<String[]> historique = historique(session);
        historique.add(new String[] { "user", texte });

        try {
            String reponse = assistant.repondre(assistant.consignes(principal.getId()), historique);
            historique.add(new String[] { "model", reponse });
            resultat.put("reponse", reponse);
        } catch (BusinessException e) {
            // la question n'a pas eu de réponse : on la retire de l'historique
            historique.remove(historique.size() - 1);
            resultat.put("erreur", e.getMessage());
        }

        while (historique.size() > MAX_MESSAGES) {
            historique.remove(0);
        }
        return resultat;
    }

    @SuppressWarnings("unchecked")
    private List<String[]> historique(HttpSession session) {
        List<String[]> historique = (List<String[]>) session.getAttribute(HISTORIQUE);
        if (historique == null) {
            historique = new ArrayList<>();
            session.setAttribute(HISTORIQUE, historique);
        }
        return historique;
    }
}
