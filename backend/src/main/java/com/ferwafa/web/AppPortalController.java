package com.ferwafa.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppPortalController {

    /** Open Flutter referee web app (same system / API host). */
    @GetMapping({"/app", "/referee", "/referee/"})
    public String refereeApp() {
        return "redirect:/referee/index.html";
    }
}
