package com.glz.rest.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(LoggingFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if (logger.isDebugEnabled()) {
            servletRequest = new ContentCachingRequestWrapper((HttpServletRequest) servletRequest);
            servletResponse = new ContentCachingResponseWrapper((HttpServletResponse) servletResponse);
        }

        try {
            // call next filter in the filter chain
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            // logging request and response payloads on way back
            if (logger.isDebugEnabled()) {
                try {
                    logRequestPayloads((HttpServletRequest) servletRequest);
                } catch (Exception e) {
                    logger.error("Could not read request payload:", e);
                }
                try {
                    logResponsePayloads((HttpServletResponse) servletResponse);
                } catch (Exception e) {
                    logger.error("Could not read response payload:", e);
                }
            }
        }
    }

    public void logRequestPayloads(HttpServletRequest request) {
        // check request has wrapped
        ContentCachingRequestWrapper wrapperRequest =
                WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);

        wrapperRequest = Optional.ofNullable(wrapperRequest)
                .orElseThrow(NullPointerException::new);

        logger.debug(
                "Request has payload uri:: [ {} ] \n header:: {} \n parameter:: [ {} ] \n body:: [ {} ]",
                wrapperRequest.getRequestURI(), getRequestHeader(wrapperRequest),
                getRequestParam(wrapperRequest), getRequestBody(wrapperRequest));
    }

    public void logResponsePayloads(HttpServletResponse response) throws IOException {
        // check response has wrapped
        ContentCachingResponseWrapper wrapperResponse =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);

        wrapperResponse = Optional.ofNullable(wrapperResponse)
                .orElseThrow(NullPointerException::new);

        logger.debug(
                "Response has payload response status:: [ {} ] \n header:: [ {} ] \n body:: [ {} ] ",
                wrapperResponse.getStatus(), getResponseHeader(wrapperResponse),
                getResponseBody(wrapperResponse));

        // since the output stream will also be consumed
        // so  have to copy the response back to the output stream
        wrapperResponse.copyBodyToResponse();
    }

    public String getRequestHeader(ContentCachingRequestWrapper wrapperRequest) {
        List<String> headerNames = Collections.list(wrapperRequest.getHeaderNames());
        return headerNames
                .stream()
                .map(name ->
                        name + ":" + Collections.list(wrapperRequest.getHeaders(name)))
                .collect(Collectors.joining(", "));
    }

    public String getRequestParam(ContentCachingRequestWrapper wrapperRequest) {
        return wrapperRequest.getParameterMap()
                .entrySet()
                .stream()
                .map(param -> param.getKey() + ":" + Arrays.toString(param.getValue()))
                .collect(Collectors.joining(", "));
    }

    public String getRequestBody(ContentCachingRequestWrapper wrapperRequest) {
        try {
            return new String(wrapperRequest.getContentAsByteArray(),
                    wrapperRequest.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            logger.error("Could not read request body", e);
        }
        return "";
    }

    public String getResponseHeader(ContentCachingResponseWrapper wrapperResponse) {
        return wrapperResponse.getHeaderNames()
                .stream()
                .map(name ->
                        name + ":" + wrapperResponse.getHeaders(name))
                .collect(Collectors.joining(", "));
    }

    public String getResponseBody(ContentCachingResponseWrapper wrapperResponse) {
        try {
            return new String(wrapperResponse.getContentAsByteArray(),
                    wrapperResponse.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            logger.error("Could not read response body", e);
        }
        return "";
    }

}
