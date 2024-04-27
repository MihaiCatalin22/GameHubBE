package com.gamehub.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForumPostResponse {
    private Long id;
    private String title;
    private String content;
    private AuthorInfo author;
    private Date creationDate;
    private long likesCount;
    private String category;
    private List<CommentDTO> comments;
}
