package com.mocs_on.controller;

import com.mocs_on.domain.Login;
import com.mocs_on.domain.Usuario;
import com.mocs_on.service.LoginDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login.html")
public class LoginController {

    @Autowired
    private LoginDAO loginDAO;

    @PostMapping
    public String login(@RequestBody Login login) {
        Usuario usuario = loginDAO.autenticar(login);
        if (usuario != null) {
            return "Login bem-sucedido! Bem-vindo, " + usuario.getNome();
        } else {
            return "Email ou senha inválidos.";
        }
    }

    @PostMapping("/registrar/{tipo}")
    public void registrar(@PathVariable String tipo, @RequestBody Usuario usuario) {
         loginDAO.salvarUsuario(usuario, tipo.toUpperCase());
    }
}