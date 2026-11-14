package com.dayzwiki.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Shooting range: an interactive damage calculator. Data lives in /data/range.json, the maths in /js/range.js.
 */
@Controller
public class RangeController {

    @GetMapping("/range")
    public String getRange() {
        return "range";
    }
}
