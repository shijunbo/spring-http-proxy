package com.marklogic.spring.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.ResponseExtractor;

public class DefaultResponseExtractor extends LoggingObject implements ResponseExtractor<Void> {

    private HttpServletResponse httpResponse;
    private String[] headerNamesToCopy;

    public DefaultResponseExtractor(HttpServletResponse httpResponse, String... headerNamesToCopy) {
        this.httpResponse = httpResponse;
        this.headerNamesToCopy = headerNamesToCopy;
    }

    @Override
    public Void extractData(ClientHttpResponse response) throws IOException {
        copyHeaders(httpResponse, response);
        InputStream body = response.getBody();
        if (body != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Copying the MarkLogic HTTP response body to the servlet HTTP response");
            }
            FileCopyUtils.copy(response.getBody(), httpResponse.getOutputStream());
        } else if (logger.isDebugEnabled()) {
            logger.debug("No body in the MarkLogic HTTP response, so not copying anything to the servlet HTTP response");
        }
        return null;
    }

    protected void copyHeaders(HttpServletResponse httpResponse, ClientHttpResponse response) {
        if (headerNamesToCopy != null) {
            for (String name : headerNamesToCopy) {
                List<String> values = response.getHeaders().get(name);
                // TODO Don't set it if it's null?
                if (logger.isDebugEnabled()) {
                    logger.debug(format("Setting servlet HTTP header '%s' to '%s'", name, values));
                }
                for (String value : values) {
                    httpResponse.addHeader(name, value);
                }
            }
        }
    }

}
