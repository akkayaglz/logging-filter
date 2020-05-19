package com.glz.rest.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;


public class LoggingFilterTest {

    private static final String TEST_GUID = "12345678123412341234123456789012";
    private static final String TEST_CHANNEL = "GAR";
    private static final String TEST_DIALECT = "TR";
    private static final String TEST_ACCEPT = "application/json";
    private static final String TEST_REQUEST_URI = "/test";
    private static final String TEST_PARAM_KEY = "key";
    private static final String TEST_PARAM_VALUE = "value";
    private static final String TEST_REQUEST_HEADER = "Accept:[application/json], "
            + "guid:[12345678123412341234123456789012], channel:[GAR], dialect:[TR]";
    private static final String TEST_RESPONSE_HEADER = "guid:[12345678123412341234123456789012],"
            + " Accept:[application/json]";
    private static final String TEST_BODY = "{\"id\":1,\"content\":\"test body content!\"}";

    private FilterChain chain;
    private Logger logger;
    private LoggingFilter loggingFilter;
    private MockHttpServletRequest mockHttpServletRequest;
    private MockHttpServletResponse mockHttpServletResponse;


    /**
     * set up necessary objects before tests.
     */
    @BeforeEach
    public void setUp() {
        chain = mock(FilterChain.class);
        logger = mock(Logger.class);
        loggingFilter = spy(new LoggingFilter());
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();

        mockHttpServletRequest.setMethod(HttpMethod.GET.name());
        mockHttpServletRequest.setRequestURI(TEST_REQUEST_URI);
        mockHttpServletRequest.addHeader(HttpHeaders.ACCEPT, TEST_ACCEPT);
        mockHttpServletRequest.addHeader("guid", TEST_GUID);
        mockHttpServletRequest.addHeader("channel", TEST_CHANNEL);
        mockHttpServletRequest.addHeader("dialect", TEST_DIALECT);
    }

    @Test
    public void testDoFilterEnvIsProd() throws Exception {
        // Given
        when(logger.isDebugEnabled()).thenReturn(true);
        // When
        loggingFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, chain);
        // Then
        verify(chain, times(1))
                .doFilter(mockHttpServletRequest, mockHttpServletResponse);
    }

    @Test
    public void testDoFilterEnvIsNotProd() throws Exception {
        // Given
        when(logger.isDebugEnabled()).thenReturn(true);
        // When
        loggingFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, chain);
        // Then
        verify(chain, times(1))
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testDoFilterGet() throws Exception {
        // Given
        mockHttpServletRequest.setMethod(HttpMethod.GET.name());
        // When
        loggingFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, chain);
        // Then
        verify(chain, times(1))
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testDoFilterPost() throws Exception {
        // Given
        mockHttpServletRequest.setMethod(HttpMethod.POST.name());
        mockHttpServletRequest.setContent(TEST_BODY.getBytes());
        // When
        loggingFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, chain);
        // Then
        assertThat(mockHttpServletRequest.getContentAsByteArray()).isEqualTo(TEST_BODY.getBytes());
        verify(chain, times(1))
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testLogRequestPayloadsException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            mockHttpServletRequest = null;
            loggingFilter.logRequestPayloads(mockHttpServletRequest);
        });
    }

    @Test
    public void testLogResponsePayloadsException() throws IOException {
        Assertions.assertThrows(NullPointerException.class, () -> {
            mockHttpServletResponse = null;
            loggingFilter.logResponsePayloads(mockHttpServletResponse);
        });
    }

    @Test
    public void testGetRequestHeader() {
        // When
        String requestHeaders = loggingFilter
                .getRequestHeader(new ContentCachingRequestWrapper(mockHttpServletRequest));
        // Then
        assertNotNull(requestHeaders);
        assertEquals(TEST_REQUEST_HEADER, requestHeaders);
    }

    @Test
    public void testGetRequestParam() {
        // Given
        mockHttpServletRequest.addParameter(TEST_PARAM_KEY, TEST_PARAM_VALUE);
        // When
        String params = loggingFilter
                .getRequestParam(new ContentCachingRequestWrapper(mockHttpServletRequest));
        // Then
        assertNotNull(params);
        assertEquals(TEST_PARAM_KEY + ":[" + TEST_PARAM_VALUE + "]", params);
    }

    @Test
    public void testGetResponseHeader() {
        // Given
        mockHttpServletResponse.addHeader("guid", TEST_GUID);
        mockHttpServletResponse.addHeader(HttpHeaders.ACCEPT, TEST_ACCEPT);
        // When
        String responseHeaders = loggingFilter
                .getResponseHeader(new ContentCachingResponseWrapper(mockHttpServletResponse));
        // Then
        assertNotNull(responseHeaders);
        assertEquals(TEST_RESPONSE_HEADER, responseHeaders);
    }

    @Test
    public void testGetResponseBody() throws IOException {
        // Given
        ContentCachingResponseWrapper wrapperResponse = new ContentCachingResponseWrapper(
                mockHttpServletResponse);
        wrapperResponse.getWriter().print(TEST_BODY);
        // When
        String responseBody = loggingFilter.getResponseBody(wrapperResponse);
        // Then
        assertEquals(TEST_BODY, responseBody);
    }

}