package com.mocs_on.service;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mocs_on.domain.Usuario;
import com.mocs_on.domain.Comite;
import com.mocs_on.security.*;

@Service
public class SecaoUsuarioService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecaoUsuarioService.class);

    private final LoginDAO loginDAO;

    @Autowired
    public SecaoUsuarioService(LoginDAO loginDAO) {
        this.loginDAO = loginDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Usuario usuario = loginDAO.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado com o email: " + email));

        CargoEnum cargo = resolveCargo(usuario);

        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + cargo.name())
        );

        List<Comite> comites = usuario.getComites();

        return new SecaoUsuario(
            usuario.getEmail(),
            usuario.getSenha(),
            authorities,
            true,
            usuario.getNome(),
            cargo,
            comites
        );
    }

    private CargoEnum resolveCargo(Usuario usuario) {
        try {
            CargoEnum tipo = usuario.getTipo();
            return tipo != null ? tipo : CargoEnum.VISITANTE;
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Tipo de cargo invalido '{}' para o usuario {}. Aplicando VISITANTE.",
                    usuario.getTipo(),
                    usuario.getEmail());
            return CargoEnum.VISITANTE;
        }
    }
}
