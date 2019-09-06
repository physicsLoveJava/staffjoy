package org.spearhead.sms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.spearhead.common.common.api.BaseResponse;
import org.spearhead.common.common.auth.AuthConstant;
import org.spearhead.sms.SmsConstant;
import org.spearhead.sms.dto.SmsRequest;

import javax.validation.Valid;

@FeignClient(name = SmsConstant.SERVICE_NAME, path = "/v1", url = "${staffjoy.sms-service-endpoint}")
public interface SmsClient {
    @PostMapping(path = "/queue_send")
    BaseResponse send(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid SmsRequest smsRequest);
}
