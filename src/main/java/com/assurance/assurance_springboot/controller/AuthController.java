package com.assurance.assurance_springboot.controller;

import com.assurance.assurance_springboot.dto.InscriptionForm;
import com.assurance.assurance_springboot.model.Role;
import com.assurance.assurance_springboot.model.Utilisateur;
import com.assurance.assurance_springboot.repository.UtilisateurRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UtilisateurRepository utilisateurRepository,
                          PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/inscription")
    public String afficherInscription(Model model) {
        model.addAttribute("inscriptionForm", new InscriptionForm());
        return "inscription";
    }

    @PostMapping("/inscription")
    public String inscrire(@Valid @ModelAttribute("inscriptionForm") InscriptionForm form,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {

        if (!result.hasFieldErrors("motDePasse") && !result.hasFieldErrors("confirmation")
                && !form.getMotDePasse().equals(form.getConfirmation())) {
            result.rejectValue("confirmation", "confirmation.diff",
                    "Les mots de passe ne correspondent pas");
        }
        if (!result.hasFieldErrors("email") && utilisateurRepository.existsByEmail(form.getEmail())) {
            result.rejectValue("email", "email.existe", "Cet email est déjà utilisé");
        }
        if (result.hasErrors()) {
            return "inscription";
        }

        Utilisateur u = new Utilisateur();
        u.setNom(form.getNom());
        u.setPrenom(form.getPrenom());
        u.setEmail(form.getEmail());
        u.setTelephone(form.getTelephone());
        u.setAdresse(form.getAdresse());
        u.setPasswordHash(passwordEncoder.encode(form.getMotDePasse()));
        u.setRole(Role.CLIENT);   // un compte créé via le formulaire est toujours CLIENT
        utilisateurRepository.save(u);

        redirectAttributes.addFlashAttribute("inscriptionOk", true);
        return "redirect:/login";
    }
}