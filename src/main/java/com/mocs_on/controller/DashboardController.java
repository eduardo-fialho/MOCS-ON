package com.mocs_on.controller;
import java.sql.Connection;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mocs_on.service.AbstractDao;
import com.mocs_on.service.DashboardDao;
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
