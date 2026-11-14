package com.dayzwiki.portal.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostDto {
    private String title;
    private String content;
    /** New image files. */
    private List<MultipartFile> images;
    /** When editing: ids of the post's current images to keep (the others are removed). */
    private List<Integer> keepImageIds;
}
