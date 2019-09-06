package org.spearhead.faraday.faraday.config;

import java.util.Optional;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.spearhead.common.common.config.StaffjoyWebConfig;
import org.spearhead.common.common.env.EnvConfig;
import org.spearhead.faraday.faraday.core.balancer.LoadBalancer;
import org.spearhead.faraday.faraday.core.balancer.RandomLoadBalancer;
import org.spearhead.faraday.faraday.core.filter.HealthCheckFilter;
import org.spearhead.faraday.faraday.core.filter.NakedDomainFilter;
import org.spearhead.faraday.faraday.core.filter.SecurityFilter;
import org.spearhead.faraday.faraday.core.http.HttpClientProvider;
import org.spearhead.faraday.faraday.core.http.RequestDataExtractor;
import org.spearhead.faraday.faraday.core.http.RequestForwarder;
import org.spearhead.faraday.faraday.core.http.ReverseProxyFilter;
import org.spearhead.faraday.faraday.core.interceptor.AuthRequestInterceptor;
import org.spearhead.faraday.faraday.core.interceptor.CacheResponseInterceptor;
import org.spearhead.faraday.faraday.core.interceptor.PostForwardResponseInterceptor;
import org.spearhead.faraday.faraday.core.interceptor.PreForwardRequestInterceptor;
import org.spearhead.faraday.faraday.core.mappings.ConfigurationMappingsProvider;
import org.spearhead.faraday.faraday.core.mappings.MappingsProvider;
import org.spearhead.faraday.faraday.core.mappings.MappingsValidator;
import org.spearhead.faraday.faraday.core.mappings.ProgrammaticMappingsProvider;
import org.spearhead.faraday.faraday.core.trace.LoggingTraceInterceptor;
import org.spearhead.faraday.faraday.core.trace.ProxyingTraceInterceptor;
import org.spearhead.faraday.faraday.core.trace.TraceInterceptor;

@Configuration
@EnableConfigurationProperties({FaradayProperties.class, StaffjoyPropreties.class})
@Import(value = StaffjoyWebConfig.class)
public class FaradayConfiguration {

    protected final FaradayProperties faradayProperties;
    protected final ServerProperties serverProperties;
    protected final StaffjoyPropreties staffjoyPropreties;

    public FaradayConfiguration(FaradayProperties faradayProperties,
                                ServerProperties serverProperties,
                                StaffjoyPropreties staffjoyPropreties) {
        this.faradayProperties = faradayProperties;
        this.serverProperties = serverProperties;
        this.staffjoyPropreties = staffjoyPropreties;
    }

    @Bean
    public FilterRegistrationBean<ReverseProxyFilter> faradayReverseProxyFilterRegistrationBean(
            ReverseProxyFilter proxyFilter) {
        FilterRegistrationBean<ReverseProxyFilter> registrationBean = new FilterRegistrationBean<>(proxyFilter);
        registrationBean.setOrder(faradayProperties.getFilterOrder()); // by default to Ordered.HIGHEST_PRECEDENCE + 100
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<NakedDomainFilter> nakedDomainFilterRegistrationBean(EnvConfig envConfig) {
        FilterRegistrationBean<NakedDomainFilter> registrationBean =
                new FilterRegistrationBean<>(new NakedDomainFilter(envConfig));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 90); // before ReverseProxyFilter
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<SecurityFilter> securityFilterRegistrationBean(EnvConfig envConfig) {
        FilterRegistrationBean<SecurityFilter> registrationBean =
                new FilterRegistrationBean<>(new SecurityFilter(envConfig));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 80); // before nakedDomainFilter
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<HealthCheckFilter> healthCheckFilterRegistrationBean() {
        FilterRegistrationBean<HealthCheckFilter> registrationBean =
                new FilterRegistrationBean<>(new HealthCheckFilter());
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 70); // before faviconFilter
        return registrationBean;
    }

    @Bean
    @ConditionalOnMissingBean
    public ReverseProxyFilter faradayReverseProxyFilter(
            RequestDataExtractor extractor,
            MappingsProvider mappingsProvider,
            RequestForwarder requestForwarder,
            ProxyingTraceInterceptor traceInterceptor,
            PreForwardRequestInterceptor requestInterceptor
    ) {
        return new ReverseProxyFilter(faradayProperties, extractor, mappingsProvider,
                requestForwarder, traceInterceptor, requestInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpClientProvider faradayHttpClientProvider() {
        return new HttpClientProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestDataExtractor faradayRequestDataExtractor() {
        return new RequestDataExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public MappingsProvider faradayConfigurationMappingsProvider(EnvConfig envConfig,
                                                    MappingsValidator mappingsValidator,
                                                    HttpClientProvider httpClientProvider) {
        if (faradayProperties.isEnableProgrammaticMapping()) {
            return new ProgrammaticMappingsProvider(
                    envConfig, serverProperties,
                    faradayProperties, mappingsValidator,
                    httpClientProvider);
        } else {
            return new ConfigurationMappingsProvider(
                    serverProperties,
                    faradayProperties, mappingsValidator,
                    httpClientProvider);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadBalancer faradayLoadBalancer() {
        return new RandomLoadBalancer();
    }

    @Bean
    @ConditionalOnMissingBean
    public MappingsValidator faradayMappingsValidator() {
        return new MappingsValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestForwarder faradayRequestForwarder(
            HttpClientProvider httpClientProvider,
            MappingsProvider mappingsProvider,
            LoadBalancer loadBalancer,
            Optional<MeterRegistry> meterRegistry,
            ProxyingTraceInterceptor traceInterceptor,
            PostForwardResponseInterceptor responseInterceptor
    ) {
        return new RequestForwarder(
                serverProperties, faradayProperties, httpClientProvider,
                mappingsProvider, loadBalancer, meterRegistry,
                traceInterceptor, responseInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceInterceptor faradayTraceInterceptor() {
        return new LoggingTraceInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProxyingTraceInterceptor faradayProxyingTraceInterceptor(TraceInterceptor traceInterceptor) {
        return new ProxyingTraceInterceptor(faradayProperties, traceInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean
    public PreForwardRequestInterceptor faradayPreForwardRequestInterceptor(EnvConfig envConfig) {
        //return new NoOpPreForwardRequestInterceptor();
        return new AuthRequestInterceptor(staffjoyPropreties.getSigningSecret(), envConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostForwardResponseInterceptor faradayPostForwardResponseInterceptor() {
        //return new NoOpPostForwardResponseInterceptor();
        return new CacheResponseInterceptor();
    }
}
