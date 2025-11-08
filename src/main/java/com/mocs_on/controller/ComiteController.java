package com.mocs_on.controller;
import com.mocs_on.service.PostDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import com.mocs_on.model.Post;
@Controller
public class ComiteController {
    @GetMapping(value="/comite", params="id")
    public String mostrarPosts(Model model, @RequestParam("id") int id) throws SQLException{
        Post[] posts=PostDao.getPostsByComite(id);
        model.addAttribute("posts" posts);
        return "comite";
    }
}
