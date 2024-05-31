package main.java.org.example.httpserver.controller;

import main.java.org.example.httpserver.model.Post;
import main.java.org.example.httpserver.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/createNewPost")
    public ResponseEntity<Map<String, Object>> createNewPost(@RequestBody Post post) {
        try {
            Post savedPost = postService.createPost(post);
            String timeData = postService.fetchTimeData("Asia/Kolkata");

            Map<String, Object> response = new HashMap<>();
            response.put("db_post", savedPost);
            response.put("http_outbound", timeData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}