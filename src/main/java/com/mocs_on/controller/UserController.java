package com.mocs_on.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mocs_on.dto.InformacoesUsuarioDTO;
import com.mocs_on.security.SecaoUsuario;

@RestController
public class UserController {
    @GetMapping("/user")
    public InformacoesUsuarioDTO getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof SecaoUsuario) {
            SecaoUsuario user = (SecaoUsuario) authentication.getPrincipal();
            boolean isSecretario = user.getCargo().name().equals("SECRETARIO");
            return new InformacoesUsuarioDTO(user.getNome(), isSecretario);
        }

        return new InformacoesUsuarioDTO("Usuário Desconhecido", false);
    }
}
