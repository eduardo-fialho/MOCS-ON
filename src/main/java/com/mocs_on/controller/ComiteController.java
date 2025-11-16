package com.mocs_on.controller;
import java.sql.SQLException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mocs_on.model.Post;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Controller
@RequestMapping("/comite")
public class ComiteController {
    @GetMapping(value="/comite", params="id")
    public String mostrarPosts(Model model, @RequestParam("id") int id) throws SQLException{
        Post[] posts=PostDao.getPostsByComite(id);
        model.addAttribute("posts" posts);
        return "comite";
    }
    @GetMapping(value="/editar", params="mensagemId")
    public String editarPost(@RequestParam("mensagemId") int mensagemId, @RequestParam("novaMensagem") String novaMensagem, @PathVariable("id") int comiteId) throws SQLException{
        PostDao.setMensagem(novaMensagem, mensagemId);
        return "";
    }
    @GetMapping(value="/enviar", params="novaMensagem")
    public String enviarPost(@RequestParam("novaMensagem") String mensagem, @RequestParam("autor") String autor, @PathVariable("id") int comiteId) throws SQLException{
        PostDao.createPost(mensagem, autor, LocalDateTime.now(), "EM_ANALISE", comiteId);
        return "";
    }
}