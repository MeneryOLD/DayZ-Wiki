package com.dayzwiki.portal.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String getHome() {
        return "home";
    }

    @GetMapping("/search")
    public String getSearchPage() {
        return "search-page";
    }

}
