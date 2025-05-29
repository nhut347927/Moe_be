package com.moe.socialnetwork.api.services.impl;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.moe.socialnetwork.common.jpa.ActivityLogJpa;
import com.moe.socialnetwork.common.models.ActivityLog;
import com.moe.socialnetwork.common.models.User;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ActivityLogServiceImpl {
    private final ActivityLogJpa activityLogJPA;

    public ActivityLogServiceImpl(ActivityLogJpa activityLogJPA) {
        this.activityLogJPA = activityLogJPA;
    }

    public void logActivity(User user, String message, String data, String error) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes == null) {
                return; // Không phải HTTP context
            }

            HttpServletRequest request = attributes.getRequest();

            String requestURI = request.getRequestURI();
            String httpMethod = request.getMethod();

            // Lấy query string nếu có
            String queryString = request.getQueryString();
            String fullUri = queryString == null ? requestURI : requestURI + "?" + queryString;

            // Lấy body nếu có (từ ContentCachingRequestWrapper)
            String requestBody = null;
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    requestBody = new String(content, StandardCharsets.UTF_8);
                }
            }

            // Gắn thông tin vào message
            StringBuilder fullMessage = new StringBuilder();
            fullMessage.append("[").append(httpMethod).append(" ").append(fullUri).append("] ");
            fullMessage.append(message);

            // Gắn thêm body vào data nếu cần
            if (requestBody != null && !requestBody.isBlank()) {
                data = "RequestBody: " + requestBody + (data != null ? "\n" + data : "");
            }

            ActivityLog log = new ActivityLog();
            log.setMessage(fullMessage.toString());
            log.setUserId(user == null ? null : user.getId());
            log.setUser(user);
            log.setData(data);
            log.setError(error);
            activityLogJPA.save(log);

        } catch (Exception e) {
            throw new RuntimeException("Error while logging activity: " + e.getMessage(), e);
        }
    }
}