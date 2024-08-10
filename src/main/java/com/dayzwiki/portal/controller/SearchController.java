package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.api.PaginatedResponseApi;
import com.dayzwiki.portal.dto.api.RequestApi;
import com.dayzwiki.portal.dto.api.SearchApi;
import com.dayzwiki.portal.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchRepository searchRepository;

    @PostMapping
    public PaginatedResponseApi<SearchApi> search(@RequestBody RequestApi request) {
        List<SearchApi> results = searchRepository.search(
                request.getSearch().getValue(),
                request.getType(),
                request.getPage(),
                request.getSize(),
                request.getOrders()
        );
        int totalResults = searchRepository.getCount(request.getSearch().getValue(), request.getType());
        return new PaginatedResponseApi<>(results, totalResults, request.getPage(), request.getSize());
    }

}