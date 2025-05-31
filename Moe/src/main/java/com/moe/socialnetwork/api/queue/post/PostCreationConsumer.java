package com.moe.socialnetwork.api.queue.post;

import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moe.socialnetwork.api.services.impl.PostServiceImpl;
import com.moe.socialnetwork.common.jpa.UserJpa;
import com.moe.socialnetwork.common.models.User;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostCreationConsumer {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final PostServiceImpl postServiceImpl;
    private final UserJpa userJpa;

    private static final String QUEUE_NAME = "post_creation_queue";

    @PostConstruct
    public void startConsumerThread() {
        Thread thread = new Thread(this::consume, "post-queue-consumer");
        thread.setDaemon(true);
        thread.start();
        log.info("[PostQueue] Consumer thread started.");
    }

    private void consume() throws RedisConnectionFailureException {
        while (true) {
            try {
                // Chờ 5s lấy item, tránh treo vĩnh viễn gây timeout
                String json = redisTemplate.opsForList().leftPop(QUEUE_NAME, 5, TimeUnit.SECONDS);
                System.out.println("[PostQueue] Consuming message: " + json);
                if (json != null) {

                    PostCreationMessage message = objectMapper.readValue(json, PostCreationMessage.class);

                    User user = userJpa.findById(message.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found: " + message.getUserId()));

                    postServiceImpl.createNewPost(message.getDto(), user);

                }

            } catch (DataAccessException redisEx) {
                System.out.println("[PostQueue] Redis error: " + redisEx);
                sleep(5000);
            } catch (Exception e) {
                System.out.println("[PostQueue] Error while processing post creation: " + e); // <-- thêm dòng này
                sleep(2000);
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
