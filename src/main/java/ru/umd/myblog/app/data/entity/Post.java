package ru.umd.myblog.app.data.entity;

import lombok.*;

import java.util.Set;

@Data
public class Post {
    private long id;

    private String title;

    private String imageUrl;

    private String content;

    private Set<String> tags;

    private int likes;
}
