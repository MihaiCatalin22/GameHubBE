package com.Gamehub.backend.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ForumPostResponse {
    private Long id;
    private String title;
    private String content;
    private AuthorInfo author;
    private Date creationDate;
    private long likesCount;
    private String category;
}
