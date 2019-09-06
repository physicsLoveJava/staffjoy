package org.spearhead.web.service;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.stereotype.Service;
import org.spearhead.common.common.error.ServiceException;

@Service
public class HelperService {

    static final ILogger logger = SLoggerFactory.getLogger(HelperService.class);

    static final String METHOD_POST = "POST";

    public static boolean isPost(HttpServletRequest request) {
        return METHOD_POST.equals(request.getMethod());
    }

    public void logError(ILogger log, String errMsg) {
        log.error(errMsg);
    }

    public void logException(ILogger log, Exception ex, String errMsg) {
        log.error(errMsg, ex);
    }

    public static String buildUrl(String scheme, String host) {
        return buildUrl(scheme, host, null);
    }

    public static String buildUrl(String scheme, String host, String path) {
        try {
            URI uri = new URI(scheme, host, path, null);
            return uri.toString();
        } catch (URISyntaxException ex) {
            String errMsg = "Internal uri parsing exception.";
            logger.error(errMsg);
            throw new ServiceException(errMsg, ex);
        }
    }
}
