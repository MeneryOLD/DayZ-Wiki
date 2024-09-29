package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "diseases")
@Getter
public class Diseases {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String englishName;
    private String altName;

    private String causes;
    private String symptoms;
    private String treatment;
    private String source;
    private String description;

    public String getUrlName() {
        String formattedName = englishName.replace(" ", "_").toLowerCase();
        return "http://localhost:8080/diseases/" + formattedName; /*https://dayzwiki.net/diseases/*/
    }

}

