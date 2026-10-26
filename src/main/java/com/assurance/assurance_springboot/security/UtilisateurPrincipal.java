package com.assurance.assurance_springboot.security;

import com.assurance.assurance_springboot.model.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UtilisateurPrincipal implements UserDetails {

    private final Utilisateur utilisateur;

    public UtilisateurPrincipal(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public Long getId() {
        return utilisateur.getId();
    }

    public String getPrenom() {
        return utilisateur.getPrenom();
    }

    public String getNom() {
        return utilisateur.getNom();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ROLE_CLIENT, ROLE_AGENT ou ROLE_ADMIN
        return List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()));
    }

    @Override
    public String getPassword() {
        return utilisateur.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return utilisateur.getEmail();
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}