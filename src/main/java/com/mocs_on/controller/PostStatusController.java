package com.mocs_on.controller;

import java.sql.Connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mocs_on.domain.Post;
import com.mocs_on.service.HDataSource;
import com.mocs_on.service.PostDAO;

@RequestMapping("/postStatus")
public class PostStatusController {

    @Autowired
	private HDataSource ds;

    @RequestMapping("/aceitar/{id}")
    public String aceitarPost(@PathVariable("id") Long idPost) throws Exception {
        try (Connection conn = ds.getConnection()) {
			PostDAO PostDao = new PostDAO();
            
            Post post = PostDao.get(conn, idPost);

            post.setStatus(Post.TipoPost.PUBLICO);

            PostDao.update(conn, post);
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

            post.setStatus(Post.TipoPost.EXCLUIDO);

            PostDao.update(conn, post);
		}

        catch (Exception e) {
            throw new Exception("Sql Exception: " + e.getMessage());
        }

    return "/comite/listar";
    }
}
