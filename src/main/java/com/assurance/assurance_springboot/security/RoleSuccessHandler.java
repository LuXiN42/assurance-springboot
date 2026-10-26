package com.assurance.assurance_springboot.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String cible = "/client/accueil";

        for (GrantedAuthority autorite : authentication.getAuthorities()) {
            String role = autorite.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                cible = "/admin/accueil";
                break;
            } else if (role.equals("ROLE_AGENT")) {
                cible = "/agent/accueil";
                break;
            }
        }

        response.sendRedirect(request.getContextPath() + cible);
    }
}