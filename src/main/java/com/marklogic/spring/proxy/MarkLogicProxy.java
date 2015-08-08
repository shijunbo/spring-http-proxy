package com.marklogic.spring.proxy;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

public class MarkLogicProxy extends LoggingObject {

    private RestTemplate restTemplate;
    private String host;
    private int port;

    public MarkLogicProxy(RestTemplate restTemplate, String host, int port) {
        this.restTemplate = restTemplate;
        this.host = host;
        this.port = port;
    }

    /**
     * Proxy a request without copying any headers.
     * 
     * @param httpRequest
     * @param httpResponse
     */
    public void proxy(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        proxy(httpRequest.getServletPath(), httpRequest, httpResponse);
    }

    /**
     * Proxy a request and copy the given headers on both the request and the response.
     * 
     * @param httpRequest
     * @param httpResponse
     * @param headerNamesToCopy
     */
    public void proxy(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String... headerNamesToCopy) {
        proxy(httpRequest.getServletPath(), httpRequest, httpResponse, new DefaultRequestCallback(httpRequest,
                headerNamesToCopy), new DefaultResponseExtractor(httpResponse, headerNamesToCopy));
    }

    /**
     * Proxy a request, using the given path instead of the servlet path in the HttpServletRequest.
     * 
     * @param path
     * @param httpRequest
     * @param httpResponse
     * @param headerNamesToCopy
     */
    public void proxy(String path, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            String... headerNamesToCopy) {
        proxy(path, httpRequest, httpResponse, new DefaultRequestCallback(httpRequest, headerNamesToCopy),
                new DefaultResponseExtractor(httpResponse, headerNamesToCopy));
    }

    /**
     * Specify your own request callback and response extractor. This gives you the most flexibility, but does the least
     * for you.
     * 
     * @param path
     * @param httpRequest
     * @param httpResponse
     * @param requestCallback
     * @param responseExtractor
     * @return
     */
    public <T> T proxy(String path, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) {
        URI uri = buildUri(httpRequest, host, port, path);

        if (logger.isInfoEnabled()) {
            logger.info(format("Proxying to URI: %s", uri));
        }

        HttpMethod method = determineMethod(httpRequest);
        return restTemplate.execute(uri, method, requestCallback, responseExtractor);
    }

    protected HttpMethod determineMethod(HttpServletRequest request) {
        return HttpMethod.valueOf(request.getMethod());
    }

    protected URI buildUri(HttpServletRequest httpRequest, String host, int port, String path) {
        try {
            return new URI("http", null, host, port, path, httpRequest.getQueryString(), null);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Unable to build URI, cause: " + ex.getMessage(), ex);
        }
    }
}
