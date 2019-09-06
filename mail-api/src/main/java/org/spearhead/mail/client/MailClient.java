package org.spearhead.mail.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.spearhead.common.common.api.BaseResponse;
import org.spearhead.mail.MailConstant;
import org.spearhead.mail.dto.EmailRequest;

import javax.validation.Valid;

@FeignClient(name = MailConstant.SERVICE_NAME, path = "/v1", url = "${staffjoy.email-service-endpoint}")
public interface MailClient {
    @PostMapping(path = "/send")
    BaseResponse send(@RequestBody @Valid EmailRequest request);
}
