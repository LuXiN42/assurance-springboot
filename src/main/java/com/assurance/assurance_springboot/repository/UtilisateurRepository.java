package com.assurance.assurance_springboot.repository;

import com.assurance.assurance_springboot.model.Role;
import com.assurance.assurance_springboot.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table {@code utilisateurs}.
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Utilisateur> findByRole(Role role);

    List<Utilisateur> findByRoleOrderByNomAscPrenomAsc(Role role);

    long countByRole(Role role);
}
