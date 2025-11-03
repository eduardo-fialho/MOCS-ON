package com.mocs_on.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mocs_on.domain.Comite;

public class SecaoUsuario implements UserDetails, Serializable { 

    private static final long serialVersionUID = 1L; 

    private final String username; 
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final String nome; 
    private final CargoEnum cargo;
    private final List<Comite> comites;

    public SecaoUsuario(String username, String password, Collection<? extends GrantedAuthority> authorities, boolean enabled, String nome, CargoEnum cargo, List<Comite> comites) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.nome = nome;
        this.cargo = cargo;
        this.comites = comites != null ? Collections.unmodifiableList(comites) : Collections.emptyList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { 
        return authorities; 
    }

    @Override
    public String getPassword() { 
        return password; 
    }

    @Override
    public String getUsername() { 
        return username; 
    }

    @Override
    public boolean isEnabled() { 
        return enabled; 
    }
    
    @Override
    public boolean isAccountNonExpired() { 
        return true; 
    }

    @Override
    public boolean isAccountNonLocked() { 
        return true; 
    }

    @Override
    public boolean isCredentialsNonExpired() { 
        return true; 
    }

    public String getNome() {
        return nome;
    }

    public CargoEnum getCargo() {
        return cargo;
    }

    public List<Comite> getComites() {
        return comites;
    }
}