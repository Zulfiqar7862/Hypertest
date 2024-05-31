package main.java.org.example.httpserver.service;

import main.java.org.example.httpserver.model.Post;
public interface PostService {
    Post createPost(Post post);
    String fetchTimeData(String timezone);
}