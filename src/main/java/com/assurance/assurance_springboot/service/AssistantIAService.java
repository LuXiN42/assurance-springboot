package com.assurance.assurance_springboot.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.Garantie;
import com.assurance.assurance_springboot.model.Sinistre;
import com.assurance.assurance_springboot.model.Utilisateur;
import com.assurance.assurance_springboot.repository.ContratRepository;
import com.assurance.assurance_springboot.repository.SinistreRepository;
import com.assurance.assurance_springboot.repository.UtilisateurRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Bibi : envoie la conversation du client à l'API Gemini et renvoie la réponse.
 * La clé est lue dans ia.properties (ignoré par Git) ou dans la variable GEMINI_API_KEY.
 */
@Service
public class AssistantIAService {

    // si le premier modèle est surchargé (erreur 503), on réessaie avec le suivant
    private static final String[] MODELES = { "gemini-flash-latest", "gemini-flash-lite-latest" };
    private static final String URL_API = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ContratRepository contrats;
    private final SinistreRepository sinistres;
    private final UtilisateurRepository utilisateurs;
    private final String cle;

    public AssistantIAService(ContratRepository contrats, SinistreRepository sinistres,
            UtilisateurRepository utilisateurs, @Value("${gemini.api.key:${GEMINI_API_KEY:}}") String cle) {
        this.contrats = contrats;
        this.sinistres = sinistres;
        this.utilisateurs = utilisateurs;
        this.cle = cle;
    }

    /**
     * @param consignes  le texte qui explique à l'IA son rôle (+ les infos du client)
     * @param historique les messages déjà échangés : {"user", texte} ou {"model", texte}
     */
    public String repondre(String consignes, List<String[]> historique) {
        if (cle == null || cle.isBlank() || cle.startsWith("COLLE_TA_CLE")) {
            throw new BusinessException("Bibi n'est pas configuré (clé API manquante).");
        }

        JsonObject corps = new JsonObject();

        JsonObject systeme = new JsonObject();
        systeme.add("parts", texte(consignes));
        corps.add("system_instruction", systeme);

        JsonArray contents = new JsonArray();
        for (String[] message : historique) {
            JsonObject m = new JsonObject();
            m.addProperty("role", message[0]);
            m.add("parts", texte(message[1]));
            contents.add(m);
        }
        corps.add("contents", contents);

        HttpResponse<String> reponse = null;
        for (String modele : MODELES) {
            HttpRequest requete = HttpRequest.newBuilder(URI.create(URL_API + modele + ":generateContent"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", cle.trim())
                    .POST(HttpRequest.BodyPublishers.ofString(corps.toString()))
                    .build();
            try {
                reponse = http.send(requete, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                System.err.println("Modèle " + modele + " injoignable : " + e);
                reponse = null;
                continue;
            }
            if (reponse.statusCode() != 503) {
                break;
            }
            System.err.println("Modèle " + modele + " surchargé (503), on essaie le suivant.");
        }

        if (reponse == null) {
            throw new BusinessException("Impossible de joindre Bibi. Réessayez plus tard.");
        }
        if (reponse.statusCode() != 200) {
            System.err.println("Erreur Gemini " + reponse.statusCode() + " : " + reponse.body());
            throw new BusinessException("Bibi est indisponible pour le moment.");
        }

        // la réponse se trouve dans candidates[0].content.parts[0].text
        try {
            JsonObject json = JsonParser.parseString(reponse.body()).getAsJsonObject();
            return json.getAsJsonArray("candidates").get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts").get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (RuntimeException e) {
            System.err.println("Réponse Gemini illisible : " + reponse.body());
            throw new BusinessException("Bibi n'a pas pu répondre à cette question.");
        }
    }

    // texte envoyé à l'IA pour lui expliquer son rôle et lui donner les infos du client
    @Transactional(readOnly = true)
    public String consignes(Long clientId) {
        Utilisateur client = utilisateurs.findById(clientId)
                .orElseThrow(() -> new BusinessException("Client introuvable."));

        StringBuilder sb = new StringBuilder();
        sb.append("Tu es Bibi, l'assistant virtuel de B'Care, une compagnie d'assurance. ");
        sb.append("Tu réponds en français, avec des phrases courtes et simples, sans mise en forme (pas de gras ni de listes avec des étoiles). ");
        sb.append("Tu aides le client à comprendre ses contrats, ses garanties, ses sinistres et ses paiements, ");
        sb.append("et tu peux lui expliquer comment déclarer un sinistre depuis son espace. ");
        sb.append("N'invente aucune information : si tu ne sais pas, conseille-lui de contacter un agent. ");
        sb.append("Si la question n'a rien à voir avec l'assurance, dis-le poliment.\n\n");

        sb.append("Client : ").append(client.getPrenom()).append(" ").append(client.getNom()).append("\n");

        List<Contrat> liste = contrats.findByClientIdOrderByDateDebutDesc(clientId);
        sb.append("\nSes contrats :\n");
        if (liste.isEmpty()) {
            sb.append("- aucun contrat\n");
        }
        for (Contrat c : liste) {
            sb.append("- n° ").append(c.getNumeroContrat())
              .append(", ").append(c.getTypeAssurance().getNom())
              .append(", statut ").append(c.getStatut())
              .append(", prime ").append(c.getPrime()).append(" €")
              .append(", du ").append(c.getDateDebut())
              .append(" au ").append(c.getDateFin() == null ? "(pas de fin)" : c.getDateFin());
            List<String> noms = new ArrayList<>();
            for (Garantie g : c.getGaranties()) {
                noms.add(g.getNom());
            }
            sb.append(", garanties : ").append(noms.isEmpty() ? "aucune" : String.join(", ", noms)).append("\n");
        }

        List<Sinistre> dossiers = sinistres.findByContratClientIdOrderByDateDesc(clientId);
        sb.append("\nSes sinistres :\n");
        if (dossiers.isEmpty()) {
            sb.append("- aucun sinistre\n");
        }
        for (Sinistre s : dossiers) {
            sb.append("- n° ").append(s.getNumeroSinistre())
              .append(" du ").append(s.getDate())
              .append(", contrat ").append(s.getContrat().getNumeroContrat())
              .append(", statut ").append(s.getStatut());
            if (s.getTypeSinistre() != null) {
                sb.append(", type ").append(s.getTypeSinistre());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // Gemini attend le texte sous la forme "parts": [ { "text": "..." } ]
    private JsonArray texte(String contenu) {
        JsonObject part = new JsonObject();
        part.addProperty("text", contenu);
        JsonArray parts = new JsonArray();
        parts.add(part);
        return parts;
    }
}
