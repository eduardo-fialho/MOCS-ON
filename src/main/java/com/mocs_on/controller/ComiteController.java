package com.mocs_on.controller;

import com.mocs_on.domain.Comite;
import com.mocs_on.domain.Usuario;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.HDataSource;
import com.mocs_on.service.AlunoDao;
import com.mocs_on.service.ComiteDao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import java.sql.SQLException;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequestMapping("/comite")
public class ComiteController {

        
    @Autowired
	private HDataSource ds;

    @GetMapping("/criar")
    public String criarComite(Model model, HttpSession session) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof SecaoUsuario) {
            SecaoUsuario secaoUsuario = (SecaoUsuario) principal;
            String cargoUsuario = secaoUsuario.getCargo().name();
        
            if (cargoUsuario.equals("VISITANTE") || cargoUsuario.equals("IMPRENSA") || cargoUsuario.equals("APOIADOR") || cargoUsuario.equals("EQUIPE")) {
                throw new Exception("Acesso negado");
            }

            //else {
            //    throw new Exception("Usuário não autenticado ou tipo inválido.");
            //}
        }

        return "criar_comite";
    }

    @PostMapping("/salvar")
    public String salvarComite(@ModelAttribute Comite comite) throws Exception {
        try (Connection conn = ds.getConnection()){
            
            ComiteDao.insert(conn, comite);
            conn.commit();
        }
        
        catch (Exception e) {
            throw new Exception("Erro ao salvar comitê: " + e.getMessage());
        }
        
        return "redirect:/comite/listar";
    }

    @RequestMapping("/listar")
    public String listarComites(Model model, HttpSession session) throws Exception {
        try (Connection conn = ds.getConnection()) {
			List<Comite> comites = ComiteDao.listComites(conn);
			model.addAttribute("comites", comites);
		}

        catch (Exception e) {
            throw new Exception("Erro ao listar comitês: " + e.getMessage());
        }

        return "ver_comites";
    }

    @RequestMapping("/editar/{id}")
    public String editarComite(@PathVariable("id") Long idComite, Model model, HttpSession session) throws Exception, SQLException {
        try (Connection conn = ds.getConnection()) {
			Comite comite = ComiteDao.get(conn, idComite);

            model.addAttribute("comite", comite);
		}

        catch (SQLException e) {
            throw new SQLException("Sql Exception: " + e.getMessage());
        }

        catch (Exception e) {
            throw new Exception("Erro ao listar comitês: " + e.getMessage());
        }

        return "editar_comite";
    }

    @PostMapping("/editar")
    public String salvarEditarComite(@ModelAttribute Comite comite) throws Exception, SQLException {
        try (Connection conn = ds.getConnection()) {
            ComiteDao.update(conn, comite);
            conn.commit();
		}

        catch (SQLException e) {
            throw new SQLException("Sql Exception: " + e.getMessage());
        }

        catch (Exception e) {
            throw new Exception("Erro: " + e.getMessage());
        }

        return "redirect:/comite/listar";
    }

    @RequestMapping("/editar/deletar-comite/{id}")
    public String deletarComite(@PathVariable("id") Long comiteId) throws Exception, SQLException {
        try (Connection conn = ds.getConnection()) {
            Comite comite = ComiteDao.get(conn, comiteId);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof SecaoUsuario) {
                SecaoUsuario secaoUsuario = (SecaoUsuario) principal;
                String cargoUsuario = secaoUsuario.getCargo().name();
            
                if (cargoUsuario.equals("VISITANTE") || cargoUsuario.equals("IMPRENSA") || cargoUsuario.equals("APOIADOR") || cargoUsuario.equals("EQUIPE")) {
                    throw new Exception("Acesso negado");
                }
            }            

            ComiteDao.delete(conn, comiteId);
            conn.commit();
		}

        catch (SQLException e) {
            throw new SQLException("Sql Exception: " + e.getMessage());
        }

        catch (Exception e) {
            throw new Exception("Erro: " + e.getMessage());
        }

        return "redirect:/comite/listar";
    }
}