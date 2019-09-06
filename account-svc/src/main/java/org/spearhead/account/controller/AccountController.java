package org.spearhead.account.controller;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.spearhead.account.dto.AccountDto;
import org.spearhead.account.dto.AccountList;
import org.spearhead.account.dto.EmailChangeRequest;
import org.spearhead.account.dto.GenericAccountResponse;
import org.spearhead.account.dto.ListAccountResponse;
import org.spearhead.account.dto.PasswordResetRequest;
import org.spearhead.account.dto.TrackEventRequest;
import org.spearhead.account.dto.UpdatePasswordRequest;
import org.spearhead.account.dto.VerifyPasswordRequest;
import org.spearhead.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.spearhead.common.common.api.BaseResponse;
import org.spearhead.common.common.auth.AuthConstant;
import org.spearhead.common.common.auth.AuthContext;
import org.spearhead.common.common.auth.Authorize;
import org.spearhead.common.common.auth.PermissionDeniedException;
import org.spearhead.common.common.env.EnvConfig;
import org.spearhead.common.common.env.EnvConstant;
import org.spearhead.common.common.error.ServiceException;
import org.spearhead.common.common.validation.PhoneNumber;

@RestController
@RequestMapping("/v1/account")
@Validated
public class AccountController {

    static final ILogger logger = SLoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private EnvConfig envConfig;

    @GetMapping(path = "/get_account_by_phonenumber")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_COMPANY_SERVICE
    })
    public GenericAccountResponse getAccountByPhonenumber(@RequestParam @PhoneNumber String phoneNumber) {
        AccountDto accountDto = accountService.getAccountByPhoneNumber(phoneNumber);
        GenericAccountResponse genericAccountResponse = new GenericAccountResponse(accountDto);
        return genericAccountResponse;
    }

    @GetMapping(path = "/list")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public ListAccountResponse listAccounts(@RequestParam int offset, @RequestParam @Min(0) int limit) {
        AccountList accountList = accountService.list(offset, limit);
        ListAccountResponse listAccountResponse = new ListAccountResponse(accountList);
        return listAccountResponse;
    }

    @GetMapping(path = "/get")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE,
            AuthConstant.AUTHORIZATION_COMPANY_SERVICE,
            AuthConstant.AUTHORIZATION_WHOAMI_SERVICE,
            AuthConstant.AUTHORIZATION_BOT_SERVICE,
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_SUPERPOWERS_SERVICE
    })
    public GenericAccountResponse getAccount(@RequestParam @NotBlank String userId) {
        this.validateAuthenticatedUser(userId);
        this.validateEnv();

        AccountDto accountDto = accountService.get(userId);

        GenericAccountResponse genericAccountResponse = new GenericAccountResponse(accountDto);
        return genericAccountResponse;
    }

    @PutMapping(path = "/update_password")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse updatePassword(@RequestBody @Valid UpdatePasswordRequest request) {
        this.validateAuthenticatedUser(request.getUserId());

        accountService.updatePassword(request.getUserId(), request.getPassword());

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("password updated");

        return baseResponse;
    }

    @PostMapping(path = "/verify_password")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericAccountResponse verifyPassword(@RequestBody @Valid VerifyPasswordRequest request) {
        AccountDto accountDto = accountService.verifyPassword(request.getEmail(), request.getPassword());

        GenericAccountResponse genericAccountResponse = new GenericAccountResponse(accountDto);
        return genericAccountResponse;
    }

    // RequestPasswordReset sends an email to a user with a password reset link
    @PostMapping(path = "/request_password_reset")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse requestPasswordReset(@RequestBody @Valid PasswordResetRequest request) {
        accountService.requestPasswordReset(request.getEmail());

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("password reset requested");

        return baseResponse;
    }

    // RequestPasswordReset sends an email to a user with a password reset link
    @PostMapping(path = "/request_email_change")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse requestEmailChange(@RequestBody @Valid EmailChangeRequest request) {
        this.validateAuthenticatedUser(request.getUserId());

        accountService.requestEmailChange(request.getUserId(), request.getEmail());

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("email change requested");

        return baseResponse;
    }

    @PostMapping(path = "/track_event")
    public BaseResponse trackEvent(@RequestBody @Valid TrackEventRequest request) {
        accountService.trackEvent(request.getUserId(), request.getEvent());

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("event tracked");

        return baseResponse;
    }

    private void validateAuthenticatedUser(String userId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            String currentUserId = AuthContext.getUserId();
            if (StringUtils.isEmpty(currentUserId)) {
                throw new ServiceException("failed to find current user id");
            }
            if (!userId.equals(currentUserId)) {
                throw new PermissionDeniedException("You do not have access to this service");
            }
        }
    }

    private void validateEnv() {
        if (AuthConstant.AUTHORIZATION_SUPERPOWERS_SERVICE.equals(AuthContext.getAuthz())) {
            if (!EnvConstant.ENV_DEV.equals(this.envConfig.getName())) {
                logger.warn("Development service trying to connect outside development environment");
                throw new PermissionDeniedException("This service is not available outside development environments");
            }
        }
    }
}
