package com.dayzwiki.portal.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {
    @GetMapping({
            "/error",
            "/error.html",
            "/401",
            "/403",
            "/404",
            "/500"
    })
    public String handleError() {
        return "error";
    }
}