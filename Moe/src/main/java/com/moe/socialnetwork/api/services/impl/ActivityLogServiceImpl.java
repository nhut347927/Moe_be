package com.moe.socialnetwork.api.services.impl;

import org.springframework.stereotype.Service;

import com.moe.socialnetwork.common.jpa.ActivityLogJpa;
import com.moe.socialnetwork.common.models.ActivityLog;
import com.moe.socialnetwork.common.models.ActivityLog.LogActionType;
import com.moe.socialnetwork.common.models.ActivityLog.LogTargetType;
import com.moe.socialnetwork.common.models.User;


@Service
public class ActivityLogServiceImpl {

    private ActivityLogJpa activityLogJPA;

    public ActivityLogServiceImpl(ActivityLogJpa activityLogJPA) {
        this.activityLogJPA = activityLogJPA;
    }

    public void logActivity(User user, LogActionType action, LogTargetType targetType, String data,
            String error) {
        try {

            ActivityLog log = new ActivityLog();
            log.setLogAction(action);
            log.setLogTargetType(targetType);
            log.setUserId(user.getId());
            log.setUser(user);
            log.setData(data);
            log.setError(error);
            activityLogJPA.save(log);
        } catch (Exception e) {
            throw new RuntimeException("Error while logging activity: " + e.getMessage(), e);
        }
    }
}