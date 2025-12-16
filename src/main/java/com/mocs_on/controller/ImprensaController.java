package com.mocs_on.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/imprensa")
public class ImprensaController {

    @GetMapping
    public String paginaImprensa() {
        return "imprensa";
    }
}
