package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.dto.DeclarationSinistreForm;
import com.assurance.assurance_springboot.model.Contrat;
import com.assurance.assurance_springboot.model.Paiement;
import com.assurance.assurance_springboot.model.StatutContrat;
import com.assurance.assurance_springboot.model.Sinistre;
import com.assurance.assurance_springboot.model.StatutPaiement;
import com.assurance.assurance_springboot.security.UtilisateurPrincipal;
import com.assurance.assurance_springboot.service.BusinessException;
import com.assurance.assurance_springboot.service.ClientEspaceService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

@Controller
@RequestMapping("/client")
public class ClientController {
    private final ClientEspaceService service;
    public ClientController(ClientEspaceService service) { this.service = service; }

    @GetMapping("/accueil")
    public String accueil(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        Long id = principal.getId();
        model.addAttribute("prenom", principal.getPrenom());
        model.addAttribute("nombreContrats", service.contrats(id).size());
        model.addAttribute("nombreSinistres", service.sinistres(id).size());
        model.addAttribute("nombrePaiementsEnAttente", service.paiements(id).stream().filter(p -> p.getStatut() == StatutPaiement.EN_ATTENTE).count());
        model.addAttribute("sinistresRecents", service.sinistres(id).stream().limit(5).toList());
        return "client/accueil";
    }

    @GetMapping("/contrats")
    public String contrats(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("contrats", service.contrats(principal.getId()));
        return "client/contrats";
    }

    @GetMapping("/contrats/{id}")
    public String contrat(@PathVariable Long id, @AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("contrat", service.contrat(id, principal.getId()));
        return "client/contrat-detail";
    }

    @GetMapping("/sinistres")
    public String sinistres(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("sinistres", service.sinistres(principal.getId()));
        return "client/sinistres";
    }

    @GetMapping("/sinistres/nouveau")
    public String nouveauSinistre(@RequestParam(required=false) Long contratId,
            @AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        DeclarationSinistreForm form = new DeclarationSinistreForm();
        form.setContratId(contratId);
        model.addAttribute("form", form);
        model.addAttribute("contrats", service.contrats(principal.getId()).stream()
                .filter(c -> c.getStatut() == StatutContrat.ACTIF).toList());
        model.addAttribute("dateMaximum", LocalDate.now());
        return "client/sinistre-form";
    }

    @PostMapping("/sinistres")
    public String declarer(@ModelAttribute("form") DeclarationSinistreForm form,
            @RequestParam(name="fichiers", required=false) MultipartFile[] fichiers,
            @AuthenticationPrincipal UtilisateurPrincipal principal, Model model, RedirectAttributes redirect) {
        try {
            Sinistre s = service.declarer(principal.getId(), form.getContratId(), form.getDate(), form.getDescription(),
                    form.getMontantEstime(), form.getTypeSinistre(), fichiers);
            redirect.addFlashAttribute("succes", "Votre déclaration a été enregistrée.");
            return "redirect:/client/sinistres/" + s.getId();
        } catch (BusinessException e) {
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("contrats", service.contrats(principal.getId()).stream()
                    .filter(c -> c.getStatut() == StatutContrat.ACTIF).toList());
            model.addAttribute("dateMaximum", LocalDate.now());
            return "client/sinistre-form";
        }
    }

    @GetMapping("/sinistres/{id}")
    public String sinistre(@PathVariable Long id, @AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("sinistre", service.sinistre(id, principal.getId()));
        return "client/sinistre-detail";
    }

    @GetMapping("/pieces/{id}/telecharger")
    public ResponseEntity<Resource> telechargerPiece(@PathVariable Long id,
            @AuthenticationPrincipal UtilisateurPrincipal principal) throws IOException {
        Path path = service.fichierPiece(id, principal.getId());
        Resource resource = new FileSystemResource(path);
        String name = path.getFileName().toString();
        String type = Files.probeContentType(path);
        MediaType mediaType = type == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(type);
        return ResponseEntity.ok().contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(name).build().toString())
                .body(resource);
    }

    @GetMapping("/paiements")
    public String paiements(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("paiements", service.paiements(principal.getId()));
        model.addAttribute("echeances", service.paiements(principal.getId()).stream()
                .filter(p -> p.getStatut() == StatutPaiement.EN_ATTENTE).toList());
        return "client/paiements";
    }

    @GetMapping("/paiements/{id}")
    public String paiement(@PathVariable Long id, @AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("paiement", service.paiement(id, principal.getId()));
        return "client/paiement-detail";
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView accesRefuse(BusinessException exception) {
        ModelAndView vue = new ModelAndView("error", HttpStatus.FORBIDDEN);
        vue.addObject("status", 403);
        return vue;
    }
}
