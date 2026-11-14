package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Fields shared by every wiki entry (item, vehicle or disease).
 */
@MappedSuperclass
@Getter
public abstract class BaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String englishName;

    /** Alternative names and in-game class names, used by search. */
    @Column(columnDefinition = "TEXT")
    private String altName;

    /** Table name of the entry; the front end uses it to pick how to render the details. */
    private String source;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Where the item can be found, e.g. "Military, Police". */
    @Column(columnDefinition = "TEXT")
    private String location;

    private String rarity;

    /** Maps the item currently spawns on, from the official Central Economy files. */
    private String maps;

    /** Primary in-game class name. */
    private String classname;

    /** Path of the item picture served from /img/items (null when the game has no published image). */
    private String image;

    protected abstract String getSection();

    public String getUrlName() {
        return "/" + getSection() + "/" + englishName.replace(" ", "_").toLowerCase();
    }
}
