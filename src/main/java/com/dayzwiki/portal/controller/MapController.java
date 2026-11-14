package com.dayzwiki.portal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Map;

/**
 * Pages for the official DayZ maps. Map content lives in /data/maps.json; per-map loot comes from /api/v1/wiki/maps/{map}.
 */
@Controller
@RequestMapping("/maps")
public class MapController {

    private static final Map<String, String> MAPS = Map.of(
            "chernarus", "Chernarus",
            "livonia", "Livonia",
            "sakhal", "Sakhal",
            "nasdara", "Nasdara"
    );

    @GetMapping
    public String getMaps() {
        return "maps";
    }

    @GetMapping("/{id}")
    public String getMap(@PathVariable String id, Model model) {
        String key = id.toLowerCase(Locale.ROOT);
        String name = MAPS.get(key);
        if (name == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown map");
        }
        model.addAttribute("mapId", key);
        model.addAttribute("mapName", name);
        return "map";
    }
}
