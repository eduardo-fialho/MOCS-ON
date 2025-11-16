package com.mocs_on.controller;
import com.mocs_on.service.PostDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import com.mocs_on.model.Post;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
@ControllerAdvice
public class ControllerHelper{
    @ExceptionHandler(SQLException.class)
    public String databaseError(){
        return "databaseError";
    }
}