package org.spearhead.faraday.faraday.core.interceptor;

import org.spearhead.faraday.faraday.config.MappingProperties;
import org.spearhead.faraday.faraday.core.http.ResponseData;

public class NoOpPostForwardResponseInterceptor implements PostForwardResponseInterceptor {
    @Override
    public void intercept(ResponseData data, MappingProperties mapping) {

    }
}
