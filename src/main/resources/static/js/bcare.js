// barre du haut : transparente sur le fond violet, blanche ensuite
var nav = document.querySelector(".nav");
var hero = document.querySelector(".hero, .espace-hero");
var apercu = document.querySelector(".apercu");

function auScroll() {
    var y = window.scrollY;
    if (hero) {
        nav.classList.toggle("sombre", y < hero.offsetHeight - 120);
        // la carte remonte un peu moins vite que la page
        if (apercu && y < hero.offsetHeight) {
            apercu.style.transform = "translateY(" + (y * -0.12) + "px)";
        }
    }
}
window.addEventListener("scroll", auScroll, { passive: true });
auScroll();

// les blocs .r apparaissent en entrant dans l'écran et repartent en sortant,
// donc l'animation se rejoue dans les deux sens
var blocs = document.querySelectorAll(".r");

blocs.forEach(function (el) {
    var freres = Array.prototype.filter.call(el.parentElement.children, function (x) {
        return x.classList.contains("r");
    });
    el.style.transitionDelay = (freres.indexOf(el) * 0.08) + "s";
});

var observateur = new IntersectionObserver(function (entrees) {
    entrees.forEach(function (e) {
        if (e.isIntersecting) {
            e.target.classList.add("vu");
            e.target.classList.remove("sort-haut");
        } else {
            e.target.classList.remove("vu");
            // sorti par le haut : il reviendra d'en haut quand on remontera
            e.target.classList.toggle("sort-haut", e.boundingClientRect.top < 0);
        }
    });
}, { threshold: 0.15 });

blocs.forEach(function (el) { observateur.observe(el); });

// fenêtre de confirmation (déconnexion)
document.querySelectorAll("[data-modale]").forEach(function (b) {
    b.addEventListener("click", function () {
        document.getElementById(b.dataset.modale).hidden = false;
    });
});
document.querySelectorAll(".modale").forEach(function (m) {
    m.addEventListener("click", function (e) {
        if (e.target.closest("[data-fermer]")) m.hidden = true;
    });
});
document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
        document.querySelectorAll(".modale").forEach(function (m) { m.hidden = true; });
    }
});

// les statuts dans les tableaux deviennent des pastilles de couleur
var statuts = {
    ACTIF: ["vert", "Actif"], EN_ATTENTE: ["orange", "En attente"], SUSPENDU: ["orange", "Suspendu"],
    EXPIRE: ["gris", "Expiré"], RESILIE: ["rouge", "Résilié"],
    DECLARE: ["orange", "Déclaré"], EN_ANALYSE: ["orange", "En analyse"], ACCEPTE: ["vert", "Accepté"],
    REFUSE: ["rouge", "Refusé"], INDEMNISE: ["vert", "Indemnisé"],
    EFFECTUE: ["vert", "Effectué"], ECHOUE: ["rouge", "Échoué"], ANNULE: ["gris", "Annulé"],
    CLIENT: ["", "Client"], AGENT: ["", "Agent"], ADMIN: ["", "Admin"]
};
document.querySelectorAll("td, .badge").forEach(function (el) {
    var s = statuts[el.textContent.trim()];
    if (s && el.children.length === 0) {
        var pastille = document.createElement("span");
        pastille.className = "statut " + s[0];
        pastille.textContent = s[1];
        el.textContent = "";
        el.appendChild(pastille);
    }
});

// lien du menu de la page en cours, quand le serveur ne l'a pas déjà marqué
var liens = document.querySelectorAll(".nav .liens a");
if (!document.querySelector(".nav .liens a.actif")) {
    var meilleur = null;
    liens.forEach(function (a) {
        var chemin = new URL(a.href).pathname;
        if (location.pathname.indexOf(chemin) === 0 && (!meilleur || chemin.length > new URL(meilleur.href).pathname.length)) {
            meilleur = a;
        }
    });
    if (meilleur) meilleur.classList.add("actif");
}

// Bibi : la conversation est gardée côté serveur, donc elle suit le client de page en page
var bibi = document.querySelector(".bibi");

if (bibi) {
    var url = bibi.dataset.url;
    var zone = bibi.querySelector(".bibi-messages");
    var suggestions = bibi.querySelector(".bibi-suggestions");
    var saisie = bibi.querySelector(".bibi-saisie");
    var champ = saisie.querySelector("input");
    var charge = false;

    function ajouter(classe, texte) {
        var div = document.createElement("div");
        div.className = "bibi-msg " + classe;
        div.textContent = texte;
        zone.appendChild(div);
        zone.scrollTop = zone.scrollHeight;
        return div;
    }

    function bonjour() {
        zone.textContent = "";
        ajouter("lui", "Bonjour, je suis Bibi, l'assistant B'Care ! Je connais vos contrats et vos sinistres. "
            + "Posez-moi votre question, ou choisissez-en une juste en dessous.");
        suggestions.hidden = false;
    }

    // avec Spring Security, chaque POST doit renvoyer le jeton CSRF de la page
    var jeton = document.querySelector('meta[name="_csrf"]');

    function poster(corps) {
        if (jeton && jeton.content) corps += "&_csrf=" + encodeURIComponent(jeton.content);
        return fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
            body: "format=json&" + corps
        }).then(function (r) { return r.json(); });
    }

    function charger() {
        if (charge) return;
        charge = true;
        bonjour();
        fetch(url + "?format=json")
            .then(function (r) { return r.json(); })
            .then(function (data) {
                data.messages.forEach(function (m) {
                    ajouter(m.role === "user" ? "moi" : "lui", m.texte);
                });
                suggestions.hidden = data.messages.length > 0;
            })
            .catch(function () { charge = false; });
    }

    function envoyer(question) {
        question = question.trim();
        if (question.length < 2 || champ.disabled) return;
        ajouter("moi", question);
        suggestions.hidden = true;
        champ.value = "";
        champ.disabled = true;

        var ecrit = document.createElement("div");
        ecrit.className = "bibi-ecrit";
        ecrit.innerHTML = "<i></i><i></i><i></i>";
        zone.appendChild(ecrit);
        zone.scrollTop = zone.scrollHeight;

        poster("question=" + encodeURIComponent(question))
            .then(function (data) {
                if (data.erreur) ajouter("erreur", data.erreur);
                else ajouter("lui", data.reponse);
            })
            .catch(function () { ajouter("erreur", "Bibi ne répond pas pour le moment, réessayez plus tard."); })
            .finally(function () {
                ecrit.remove();
                champ.disabled = false;
                champ.focus();
            });
    }

    function ouvrir(oui) {
        bibi.classList.toggle("ouvert", oui);
        try { localStorage.setItem("bibi", oui ? "1" : "0"); } catch (e) {}
        if (oui) charger();
    }

    bibi.addEventListener("click", function (e) {
        var b = e.target.closest("button");
        if (!b) return;
        var action = b.dataset.bibi;
        if (action === "ouvrir") { ouvrir(true); champ.focus(); }
        else if (action === "reduire") ouvrir(false);
        else if (action === "effacer") poster("action=effacer").then(bonjour);
        else if (b.parentElement === suggestions) envoyer(b.textContent);
    });

    saisie.addEventListener("submit", function (e) {
        e.preventDefault();
        envoyer(champ.value);
    });

    if (bibi.classList.contains("bibi-page")) {
        charger();
    } else {
        try { if (localStorage.getItem("bibi") === "1") ouvrir(true); } catch (e) {}
    }
}
