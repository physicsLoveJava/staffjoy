package org.spearhead.account.service.helper;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import com.google.common.collect.Maps;
import io.intercom.api.Event;
import io.intercom.api.User;
import io.sentry.SentryClient;
import lombok.RequiredArgsConstructor;
import org.spearhead.account.repo.AccountRepo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.spearhead.account.config.AppConfig;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.common.error.ServiceException;

@RequiredArgsConstructor
@Component
public class ServiceHelper {
    static final ILogger logger = SLoggerFactory.getLogger(ServiceHelper.class);

    private final AccountRepo accountRepo;

    private final SentryClient sentryClient;

    private final EnvConfig envConfig;

    void syncUserWithIntercom(User user, String userId) {
        try {
            Map<String, String> params = Maps.newHashMap();
            params.put("user_id", userId);

            User existing = User.find(params);

            if (existing != null) {
                User.update(user);
            } else {
                User.create(user);
            }

            logger.debug("updated intercom");
        } catch (Exception ex) {
            String errMsg = "fail to create/update user on Intercom";
            handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void trackEventAsync(String userId, String eventName) {
        if (envConfig.isDebug()) {
            logger.debug("intercom disabled in dev & test environment");
            return;
        }

        Event event = new Event()
                .setUserID(userId)
                .setEventName("v2_" + eventName)
                .setCreatedAt(Instant.now().toEpochMilli());

        try {
            Event.create(event);
        } catch (Exception ex) {
            String errMsg = "fail to create event on Intercom";
            handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        logger.debug("updated intercom");
    }

    // for time diff < 2s, treat them as almost same
    public boolean isAlmostSameInstant(Instant dt1, Instant dt2) {
        long diff = dt1.toEpochMilli() - dt2.toEpochMilli();
        diff = Math.abs(diff);
        if (diff < TimeUnit.SECONDS.toMillis(1)) {
            return true;
        }
        return false;
    }

    public void handleError(ILogger log, String errMsg) {
        log.error(errMsg);
        if (!envConfig.isDebug()) {
            sentryClient.sendMessage(errMsg);
        }
    }

    public void handleException(ILogger log, Exception ex, String errMsg) {
        log.error(errMsg, ex);
        if (!envConfig.isDebug()) {
            sentryClient.sendException(ex);
        }
    }
}
