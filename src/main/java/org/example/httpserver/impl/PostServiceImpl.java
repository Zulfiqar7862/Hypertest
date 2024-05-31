package main.java.org.example.httpserver.impl;

import main.java.org.example.httpserver.model.Post;
import main.java.org.example.httpserver.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import main.java.org.example.httpserver.service.PostService;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, RestTemplate restTemplate) {
        this.postRepository = postRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    @Override
    public String fetchTimeData(String timezone) {
        String apiUrl = "http://worldtimeapi.org/api/timezone/" + timezone;
        return restTemplate.getForObject(apiUrl, String.class);
    }
}