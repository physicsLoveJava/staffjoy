package org.spearhead.sms.controller;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.spearhead.sms.dto.SmsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.spearhead.common.common.api.BaseResponse;
import org.spearhead.common.common.api.ResultCode;
import org.spearhead.common.common.auth.AuthConstant;
import org.spearhead.common.common.auth.Authorize;
import org.spearhead.sms.props.AppProps;
import org.spearhead.sms.service.SmsSendService;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@Validated
public class SmsController {

    static final ILogger logger = SLoggerFactory.getLogger(SmsController.class);

    @Autowired
    private AppProps appProps;

    @Autowired
    private SmsSendService smsSendService;

    @PostMapping(path = "/queue_send")
    @Authorize({
            AuthConstant.AUTHORIZATION_COMPANY_SERVICE,
            AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE,
            AuthConstant.AUTHORIZATION_BOT_SERVICE
    })
    public BaseResponse send(@RequestBody @Valid SmsRequest smsRequest) {

        if (appProps.isWhiteListOnly()) {
            String whiteList = appProps.getWhiteListPhoneNumbers();
            boolean allowedToSend = !StringUtils.isEmpty(whiteList)
                    && whiteList.contains(smsRequest.getTo());
            if (!allowedToSend) {
                String msg = String.format("prevented sending to number %s due to whitelist", smsRequest.getTo());
                logger.warn(msg);
                return BaseResponse.builder().code(ResultCode.REQ_REJECT).message(msg).build();
            }
        }

        smsSendService.sendSmsAsync(smsRequest);
        String msg = String.format("sent message to %s. async", smsRequest.getTo());
        logger.debug(msg);
        return BaseResponse.builder().message(msg).build();
    }

}
