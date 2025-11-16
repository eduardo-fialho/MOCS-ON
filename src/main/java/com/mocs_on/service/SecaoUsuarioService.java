package com.mocs_on.service;

import java.util.List;
import java.util.Collections;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;

import com.mocs_on.domain.Usuario;
import com.mocs_on.domain.Comite;
import com.mocs_on.security.*;

@Service
public class SecaoUsuarioService implements UserDetailsService {

    private final LoginDAO loginDAO; 

    @Autowired
    public SecaoUsuarioService(LoginDAO loginDAO) {
        this.loginDAO = loginDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        Usuario usuario = loginDAO.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));
            
        CargoEnum cargo;
        try {
            cargo = usuario.getTipo();
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("O tipo de cargo do usuário não é válido: " + usuario.getTipo());
        }

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
}
