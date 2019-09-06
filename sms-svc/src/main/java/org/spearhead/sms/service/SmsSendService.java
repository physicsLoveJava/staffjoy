package org.spearhead.sms.service;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.spearhead.sms.config.AppConfig;
import org.spearhead.sms.dto.SmsRequest;
import org.spearhead.sms.props.AppProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SmsSendService {

    static final ILogger logger = SLoggerFactory.getLogger(SmsSendService.class);

    @Autowired
    private AppProps appProps;

    @Autowired
    private IAcsClient acsClient;

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void sendSmsAsync(SmsRequest smsRequest) {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(smsRequest.getTo());
        request.setSignName(appProps.getAliyunSmsSignName());
        request.setTemplateCode(smsRequest.getTemplateCode());
        request.setTemplateParam(smsRequest.getTemplateParam());

        try {
            SendSmsResponse response = acsClient.getAcsResponse(request);
            if ("OK".equals(response.getCode())) {
                logger.info("SMS sent - " + response.getRequestId(),
                        "to", smsRequest.getTo(),
                        "template_code", smsRequest.getTemplateCode(),
                        "template_param", smsRequest.getTemplateParam());
            } else {
                logger.error("failed to send: bad aliyun sms response " + response.getCode());
            }
        } catch (ClientException ex) {
            logger.error("failed to make aliyun sms request ", ex);
        }
    }
}
