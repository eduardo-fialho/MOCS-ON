package com.mocs_on.controller;

import com.mocs_on.domain.Login;
import com.mocs_on.domain.Usuario;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.LoginDAO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginDAO loginDAO;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public LoginController(LoginDAO loginDAO, AuthenticationManager authenticationManager) {
        this.loginDAO = loginDAO;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping 
    public ResponseEntity<String> login(@RequestBody Login login, HttpServletRequest request) {
        try {
            UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.unauthenticated(login.getEmail(), login.getSenha());
            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            String nomeUsuario = ((SecaoUsuario) authentication.getPrincipal()).getNome();
            return ResponseEntity.ok("Login bem-sucedido! Bem-vindo, " + nomeUsuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha inválidos.");
        }
    }

    @PostMapping("/registrar/{tipo}")
    public void registrar(@PathVariable String tipo, @RequestBody Usuario usuario) {
         loginDAO.salvarUsuario(usuario, tipo.toUpperCase());
    }
}