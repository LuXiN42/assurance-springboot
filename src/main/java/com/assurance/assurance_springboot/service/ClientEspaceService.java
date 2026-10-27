package com.assurance.assurance_springboot.service;

import com.assurance.assurance_springboot.model.*;
import com.assurance.assurance_springboot.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Tout ce que voit le client, toujours filtré sur son id. */
@Service
@Transactional
public class ClientEspaceService {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_FILES = 5;
    private static final List<String> EXTENSIONS = List.of("pdf", "png", "jpg", "jpeg", "doc", "docx");

    private final ContratRepository contrats;
    private final SinistreRepository sinistres;
    private final PaiementRepository paiements;
    private final UtilisateurRepository utilisateurs;
    private final PieceJustificativeRepository pieces;
    private final CommentaireRepository commentaires;
    private final PasswordEncoder passwordEncoder;
    private final Path uploadDir;

    public ClientEspaceService(ContratRepository contrats, SinistreRepository sinistres,
            PaiementRepository paiements, UtilisateurRepository utilisateurs,
            PieceJustificativeRepository pieces, CommentaireRepository commentaires,
            PasswordEncoder passwordEncoder,
            @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.contrats = contrats;
        this.sinistres = sinistres;
        this.paiements = paiements;
        this.utilisateurs = utilisateurs;
        this.pieces = pieces;
        this.commentaires = commentaires;
        this.passwordEncoder = passwordEncoder;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public Utilisateur profil(Long id) { return utilisateur(id); }

    @Transactional(readOnly = true)
    public List<Contrat> contrats(Long clientId) { return contrats.findByClientIdOrderByDateDebutDesc(clientId); }

    @Transactional(readOnly = true)
    public Contrat contrat(Long id, Long clientId) {
        return contrats.findCompletById(id)
                .filter(c -> c.getClient() != null && clientId.equals(c.getClient().getId()))
                .orElseThrow(() -> new BusinessException("Contrat introuvable."));
    }

    @Transactional(readOnly = true)
    public List<Sinistre> sinistres(Long clientId) {
        return sinistres.findByContratClientIdOrderByDateDesc(clientId);
    }

    @Transactional(readOnly = true)
    public Sinistre sinistre(Long id, Long clientId) {
        Sinistre s = sinistres.findByIdAndContratClientId(id, clientId)
                .orElseThrow(() -> new BusinessException("Sinistre introuvable."));
        s.setPieces(pieces.findBySinistreId(id));
        s.setCommentaires(commentaires.findBySinistreIdOrderByDateCreationAsc(id));
        return s;
    }

    @Transactional(readOnly = true)
    public List<Paiement> paiements(Long clientId) {
        return paiements.findByContratClientIdOrderByDateDesc(clientId);
    }

    @Transactional(readOnly = true)
    public Paiement paiement(Long id, Long clientId) {
        return paiements.findById(id)
                .filter(p -> p.getContrat() != null && p.getContrat().getClient() != null
                        && clientId.equals(p.getContrat().getClient().getId()))
                .orElseThrow(() -> new BusinessException("Paiement introuvable."));
    }

    public Sinistre declarer(Long clientId, Long contratId, LocalDate date, String description,
                             BigDecimal montant, String typeSinistre, MultipartFile[] fichiers) {
        if (contratId == null) throw new BusinessException("Le contrat est obligatoire.");
        Contrat contrat = contrats.findByIdEtClient(contratId, clientId)
                .orElseThrow(() -> new BusinessException("Contrat introuvable."));
        if (contrat.getStatut() != StatutContrat.ACTIF)
            throw new BusinessException("Un sinistre ne peut être déclaré que sur un contrat ACTIF.");
        if (date == null || date.isAfter(LocalDate.now()))
            throw new BusinessException("La date du sinistre est obligatoire et ne peut pas être future.");
        if (date.isBefore(contrat.getDateDebut()) || (contrat.getDateFin() != null && date.isAfter(contrat.getDateFin())))
            throw new BusinessException("La date du sinistre doit se situer dans la période du contrat.");
        if (description == null || description.trim().length() < 10 || description.trim().length() > 2000)
            throw new BusinessException("La description doit contenir de 10 à 2000 caractères.");
        if (montant != null && montant.signum() < 0)
            throw new BusinessException("Le montant estimé ne peut pas être négatif.");
        if (typeSinistre != null && typeSinistre.length() > 120)
            throw new BusinessException("Le type du sinistre ne peut dépasser 120 caractères.");
        List<MultipartFile> valides = validerFichiers(fichiers);

        Sinistre s = new Sinistre();
        s.setNumeroSinistre("SIN-" + UUID.randomUUID());
        s.setDate(date);
        s.setDescription(description.trim());
        s.setMontantEstime(montant);
        s.setTypeSinistre(vide(typeSinistre) ? null : typeSinistre.trim());
        s.setStatut(StatutSinistre.DECLARE);
        s.setContrat(contrat);
        sinistres.save(s);

        List<Path> crees = new ArrayList<>();
        try {
            Files.createDirectories(uploadDir);
            for (MultipartFile fichier : valides) {
                String original = nomNettoye(fichier.getOriginalFilename());
                String extension = extension(original);
                String stored = UUID.randomUUID() + "." + extension;
                Path target = uploadDir.resolve(stored).normalize();
                if (!target.startsWith(uploadDir)) throw new BusinessException("Nom de fichier invalide.");
                fichier.transferTo(target);
                crees.add(target);
                PieceJustificative piece = new PieceJustificative();
                piece.setNomOriginal(original);
                piece.setNomStocke(Paths.get("uploads", stored).toString().replace('\\', '/'));
                piece.setTypeMime(fichier.getContentType() == null ? null : fichier.getContentType().substring(0, Math.min(100, fichier.getContentType().length())));
                piece.setTaille(fichier.getSize());
                piece.setDateDepot(LocalDateTime.now());
                s.ajouterPiece(piece);
            }
            return sinistres.save(s);
        } catch (IOException | RuntimeException e) {
            for (Path path : crees) try { Files.deleteIfExists(path); } catch (IOException ignored) { }
            if (e instanceof BusinessException be) throw be;
            throw new BusinessException("Impossible d'enregistrer les pièces justificatives.", e);
        }
    }

    @Transactional(readOnly = true)
    public Path fichierPiece(Long pieceId, Long clientId) {
        PieceJustificative p = pieces.findOwnedById(pieceId, clientId)
                .orElseThrow(() -> new BusinessException("Pièce justificative introuvable."));
        String stored = p.getNomStocke();
        Path path;
        if (stored == null || stored.contains("..") || stored.startsWith("/") || stored.startsWith("\\"))
            throw new BusinessException("Chemin de pièce invalide.");
        String basename = Paths.get(stored).getFileName().toString();
        path = uploadDir.resolve(basename).normalize();
        if (!path.startsWith(uploadDir) || !Files.isRegularFile(path))
            throw new BusinessException("Fichier introuvable.");
        return path;
    }

    public void modifierProfil(Long id, String nom, String prenom, String email, String telephone, String adresse) {
        Utilisateur u = utilisateur(id);
        if (vide(nom) || nom.trim().length() > 100) throw new BusinessException("Le nom est obligatoire (100 caractères max).");
        if (vide(prenom) || prenom.trim().length() > 100) throw new BusinessException("Le prénom est obligatoire (100 caractères max).");
        if (email == null || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new BusinessException("Email invalide.");
        String e = email.trim().toLowerCase(Locale.ROOT);
        if (utilisateurs.existsByEmailIgnoreCaseAndIdNot(e, id)) throw new BusinessException("Cet email est déjà utilisé.");
        if (telephone != null && telephone.length() > 20) throw new BusinessException("Téléphone trop long (20 caractères max).");
        if (adresse != null && adresse.length() > 255) throw new BusinessException("Adresse trop longue (255 caractères max).");
        u.setNom(nom.trim()); u.setPrenom(prenom.trim()); u.setEmail(e);
        u.setTelephone(vide(telephone) ? null : telephone.trim()); u.setAdresse(vide(adresse) ? null : adresse.trim());
        utilisateurs.save(u);
    }

    public void changerMotDePasse(Long id, String actuel, String nouveau, String confirmation) {
        Utilisateur u = utilisateur(id);
        if (actuel == null || !passwordEncoder.matches(actuel, u.getPasswordHash()))
            throw new BusinessException("Le mot de passe actuel est incorrect.");
        if (nouveau == null || nouveau.length() < 8) throw new BusinessException("Le nouveau mot de passe doit contenir au moins 8 caractères.");
        if (!nouveau.equals(confirmation)) throw new BusinessException("La confirmation du nouveau mot de passe ne correspond pas.");
        u.setPasswordHash(passwordEncoder.encode(nouveau));
        utilisateurs.save(u);
    }

    // le profil est commun aux trois rôles
    private Utilisateur utilisateur(Long id) {
        return utilisateurs.findById(id).orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
    }

    private Utilisateur client(Long id) {
        Utilisateur u = utilisateurs.findById(id).orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
        if (u.getRole() != Role.CLIENT) throw new BusinessException("Accès client refusé.");
        return u;
    }
    private List<MultipartFile> validerFichiers(MultipartFile[] fichiers) {
        List<MultipartFile> result = new ArrayList<>();
        if (fichiers == null) return result;
        for (MultipartFile f : fichiers) {
            if (f == null || f.isEmpty()) continue;
            if (f.getSize() > MAX_FILE_SIZE) throw new BusinessException("Chaque pièce jointe doit faire 10 Mo maximum.");
            String nom = nomNettoye(f.getOriginalFilename());
            String ext = extension(nom);
            if (!EXTENSIONS.contains(ext)) throw new BusinessException("Extension non autorisée (PDF, PNG, JPG, DOC ou DOCX).");
            result.add(f);
            if (result.size() > MAX_FILES) throw new BusinessException("Vous pouvez joindre au maximum 5 fichiers.");
        }
        return result;
    }
    private String nomNettoye(String input) {
        String raw = input == null ? "fichier" : input.replace('\\', '/');
        raw = raw.substring(raw.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").replaceAll("[^\\p{L}\\p{N}._ -]", "_").trim();
        if (raw.isBlank() || raw.equals(".") || raw.equals("..")) throw new BusinessException("Nom de fichier invalide.");
        if (raw.length() > 255) raw = raw.substring(raw.length() - 255);
        return raw;
    }
    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) throw new BusinessException("Le fichier doit avoir une extension autorisée.");
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
    private boolean vide(String s) { return s == null || s.isBlank(); }
}
