package org.spearhead.mail.service;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dm.model.v20151123.SingleSendMailRequest;
import com.aliyuncs.dm.model.v20151123.SingleSendMailResponse;
import com.aliyuncs.exceptions.ClientException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.IToLog;
import com.github.structlog4j.SLoggerFactory;
import org.spearhead.mail.MailConstant;
import org.spearhead.mail.config.AppConfig;
import org.spearhead.mail.dto.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.spearhead.common.common.env.EnvConfig;
import org.spearhead.common.common.env.EnvConstant;

@Service
public class MailSendService {

    private static ILogger logger = SLoggerFactory.getLogger(MailSendService.class);

    @Autowired
    EnvConfig envConfig;

    @Autowired
    IAcsClient acsClient;

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void sendMailAsync(EmailRequest req) {
        IToLog logContext = () -> {
            return new Object[] {
                    "subject", req.getSubject(),
                    "to", req.getTo(),
                    "html_body", req.getHtmlBody()
            };
        };

        // In dev and uat - only send emails to @jskillcloud.com
        if (!EnvConstant.ENV_PROD.equals(envConfig.getName())) {
            // prepend env for sanity
            String subject = String.format("[%s] %s", envConfig.getName(), req.getSubject());
            req.setSubject(subject);

            if (!req.getTo().endsWith(MailConstant.STAFFJOY_EMAIL_SUFFIX)) {
                logger.warn("Intercepted sending due to non-production environment.");
                return;
            }
        }

        SingleSendMailRequest mailRequest = new SingleSendMailRequest();
        mailRequest.setAccountName(MailConstant.FROM);
        mailRequest.setFromAlias(MailConstant.FROM_NAME);
        mailRequest.setAddressType(1);
        mailRequest.setToAddress(req.getTo());
        mailRequest.setReplyToAddress(false);
        mailRequest.setSubject(req.getSubject());
        mailRequest.setHtmlBody(req.getHtmlBody());

        try {
            SingleSendMailResponse mailResponse = acsClient.getAcsResponse(mailRequest);
            logger.info("Successfully sent email - request id : " + mailResponse.getRequestId(), logContext);
        } catch (ClientException ex) {
            logger.error("Unable to send email ", ex, logContext);
        }
    }
}
