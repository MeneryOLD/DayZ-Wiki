package com.dayzwiki.portal.dto.api;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaginatedResponseApi<T> {
    @NonNull
    private Collection<T> items;
    @NonNull
    private long total;

    private Integer page;
    private Integer size;
    private Integer pages;

    public PaginatedResponseApi(Collection<T> items, long total, Integer page, Integer size) {
        this.items = items;
        this.total = total;
        this.page = size > -1 ? page : null;
        this.size = size > -1 ? size : null;
        this.pages = size > -1 ? (int) Math.ceil((double) total / Math.abs(size)) : null;
    }

    public PaginatedResponseApi(Collection<T> items, long total) {
        this.items = items;
        this.total = total;
    }
}