package com.mocs_on.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login.html") 
    public String login(){
        return "login";
    }

    @GetMapping("/dashboard.html") 
    public String dashBoard(){
        return "dashboard";
    }

    @GetMapping("/mesa_diretora.html")
    public String mesaDiretora() {
        return "mesa_diretora.html";
    }

    @GetMapping("/secretariado.html")
    public String secretariado() {
        return "secretariado.html";
    }
}
