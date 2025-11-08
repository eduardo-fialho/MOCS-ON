package com.mocs_on.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.mocs_on.service.ComiteDao;
import com.mocs_on.model.Comite;
import com.mocs_on.model.Post;
import java.sql.SQLException;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @RequestMapping("/")
    public String mostraDashboard(Model model) throws SQLException {
        Comite[] comites = ComiteDao.getComites();
        model.addAttribute("comite_array", comites);
        return "";

    }
}
