package com.mocs_on.controller;
import java.sql.SQLException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mocs_on.model.Post;
import com.mocs_on.service.PostDao;
@Controller
public class ComiteController {
    @GetMapping(value="/comite", params="id")
    public String mostrarPosts(Model model, @RequestParam("id") int id) throws SQLException{
        PostDao.init();
        Post[] posts=PostDao.getPostsByComite(id);
        model.addAttribute("posts", posts);
        return "comite";
    }
}
