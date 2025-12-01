package com.mocs_on.controller;

import java.sql.Connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.mocs_on.domain.Post;
import com.mocs_on.service.HDataSource;
import com.mocs_on.service.PostDAO;
import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/postStatus")
public class PostStatusController {

    @Autowired
	private HDataSource ds;

    @GetMapping("/aceitar/{id}")
    public String aceitarPost(@PathVariable("id") Long idPost, Model model, HttpSession session) throws Exception {
        try (Connection conn = ds.getConnection()) {
            PostDAO PostDao = new PostDAO();
            
            Post post = PostDao.get(conn, idPost);

            System.out.println("Status do post antes de atualizar: " + post.getStatus());
            post.setStatus(Post.TipoPost.PUBLICO);
            System.out.println("Status do post antes de atualizar: " + post.getStatus());

            PostDao.update(conn, post);
            conn.commit();
		}

        catch (Exception e) {
            throw new Exception("Sql Exception: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @RequestMapping("/negar/{id}")
    public String negarPost(@PathVariable("id") Long idPost) throws Exception {
        try (Connection conn = ds.getConnection()) {
            PostDAO PostDao = new PostDAO();
            
            Post post = PostDao.get(conn, idPost);

            System.out.println("Status do post antes de atualizar: " + post.getStatus());
            post.setStatus(Post.TipoPost.EXCLUIDO);
            System.out.println("Status do post antes de atualizar: " + post.getStatus());
            
            PostDao.update(conn, post);
            conn.commit();
		}

        catch (Exception e) {
            throw new Exception("Sql Exception: " + e.getMessage());
        }

    return "redirect:/dashboard";
    }
}
