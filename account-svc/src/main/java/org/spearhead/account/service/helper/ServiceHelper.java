package org.spearhead.account.service.helper;

import java.time.Instant;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.intercom.api.Event;
import lombok.RequiredArgsConstructor;
import org.spearhead.account.config.AppConfig;
import org.spearhead.account.repo.AccountRepo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.spearhead.common.common.env.EnvConfig;
import org.spearhead.common.common.error.ServiceException;

@RequiredArgsConstructor
@Component
public class ServiceHelper {
    static final ILogger logger = SLoggerFactory.getLogger(ServiceHelper.class);

    private final AccountRepo accountRepo;

    private final EnvConfig envConfig;

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

    public void handleError(ILogger log, String errMsg) {
        log.error(errMsg);
        if (!envConfig.isDebug()) {
        }
    }

    public void handleException(ILogger log, Exception ex, String errMsg) {
        log.error(errMsg, ex);
        if (!envConfig.isDebug()) {
        }
    }
}
