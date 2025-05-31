package com.moe.socialnetwork.api.queue.post;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moe.socialnetwork.exception.AppException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostCreationProducer {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String QUEUE_NAME = "post_creation_queue";

    public void enqueue(PostCreationMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(QUEUE_NAME, json);
        } catch (Exception e) {
            throw new AppException(QUEUE_NAME, 500);
        }
    }
}
