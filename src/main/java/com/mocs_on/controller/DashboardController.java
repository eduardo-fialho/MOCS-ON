package com.mocs_on.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.sql.*;
@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    @RequestMapping("/")
    public String mostraDashboard(Model model){
        try(Connection conn=AbstractDao.getConnection()){
            String message=DashboardDao.getMessage(conn);
            model.addAttribute("message", message);

        }catch(Exception e){

        }
        return "/dashboard";
    }
}
