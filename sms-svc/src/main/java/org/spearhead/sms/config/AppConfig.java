package org.spearhead.sms.config;

import java.util.concurrent.Executor;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.spearhead.sms.SmsConstant;
import org.spearhead.sms.props.AppProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.spearhead.common.common.config.StaffjoyRestConfig;

@Configuration
@EnableAsync
@Import(value = StaffjoyRestConfig.class)
public class AppConfig {

    public static final String ASYNC_EXECUTOR_NAME = "asyncExecutor";

    private static ILogger logger = SLoggerFactory.getLogger(AppConfig.class);

    @Autowired
    AppProps appProps;

    @Bean
    public IAcsClient acsClient() {
        IClientProfile profile = DefaultProfile.getProfile(SmsConstant.ALIYUN_REGION_ID, appProps.getAliyunAccessKey(), appProps.getAliyunAccessSecret());
        try {
            DefaultProfile.addEndpoint(SmsConstant.ALIYUN_SMS_ENDPOINT_NAME, SmsConstant.ALIYUN_REGION_ID, SmsConstant.ALIYUN_SMS_PRODUCT, SmsConstant.ALIYUN_SMS_DOMAIN);
        } catch (ClientException ex) {
            logger.error("Fail to create acsClient ", ex);
        }
        return new DefaultAcsClient(profile);
    }

    @Bean(name=ASYNC_EXECUTOR_NAME)
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(appProps.getConcurrency());
        executor.setMaxPoolSize(appProps.getConcurrency());
        executor.setQueueCapacity(SmsConstant.DEFAULT_EXECUTOR_QUEUE_CAPACITY);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
}
