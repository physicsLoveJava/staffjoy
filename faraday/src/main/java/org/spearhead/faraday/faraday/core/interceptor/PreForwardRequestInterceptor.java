package org.spearhead.faraday.faraday.core.interceptor;

import org.spearhead.faraday.faraday.config.MappingProperties;
import org.spearhead.faraday.faraday.core.http.RequestData;

public interface PreForwardRequestInterceptor {
    void intercept(RequestData data, MappingProperties mapping);
}
