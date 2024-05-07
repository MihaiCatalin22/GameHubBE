package com.gamehub.backend.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Integer rating;
    private String content;
    private String comment;
    private Date creationDate;
    private Long gameId;
    private String gameTitle;
    private AuthorInfo author;
}
