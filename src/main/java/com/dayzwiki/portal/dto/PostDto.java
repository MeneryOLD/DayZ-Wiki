package com.dayzwiki.portal.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
public class PostDto {
    private String title;
    private String content;
    private List<MultipartFile> images;
}
