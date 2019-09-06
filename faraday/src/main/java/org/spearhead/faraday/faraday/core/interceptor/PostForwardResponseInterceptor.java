package org.spearhead.faraday.faraday.core.interceptor;

import org.spearhead.faraday.faraday.config.MappingProperties;
import org.spearhead.faraday.faraday.core.http.ResponseData;

public interface PostForwardResponseInterceptor {
    void intercept(ResponseData data, MappingProperties mapping);
}
