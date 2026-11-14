package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "diseases")
@Getter
public class Diseases extends BaseItem {

    @Column(columnDefinition = "TEXT")
    private String causes;
    @Column(columnDefinition = "TEXT")
    private String symptoms;
    @Column(columnDefinition = "TEXT")
    private String treatment;
    @Column(columnDefinition = "TEXT")
    private String prevention;

    @Override
    protected String getSection() {
        return "diseases";
    }
}
