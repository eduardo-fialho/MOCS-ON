package com.mocs_on.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.mocs_on.service.ComiteDao;
import com.mocs_on.model.Comite;
import com.mocs_on.model.Post;
import java.sql.SQLException;
import java.util.ArrayList;

@Controller
public class DashboardController {

    @RequestMapping("/dashboard")
    public String mostraDashboard(Model model) throws SQLException {
        ComiteDao.init();
        ArrayList<Comite> comites = ComiteDao.getComites();
        model.addAttribute("comites", comites);
        return "dashboard";

    }
}
