package com.wky.controller;

import com.wky.service.AuthService;
import com.wky.utils.JsonUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author wky
 * @date 2025/09/03
 */
@RestController
public class ProxyController {

    private final AuthService authService;
    private final RestTemplate restTemplate;

    public ProxyController(AuthService authService, @Qualifier("restTemplate") RestTemplate restTemplate) {
        this.authService = authService;
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> proxy(
            HttpServletRequest request,
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) String body
    ) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        if (authService.verifyToken(token)) {
            String url = authService.getPayload(token, "url").toString();
            return proxyRequest(url, request, headers, body);
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    public ResponseEntity<byte[]> proxyRequest(String targetUrl, HttpServletRequest request, HttpHeaders headers, String body) {
        try {
            headers.remove(HttpHeaders.AUTHORIZATION);

            String fullUrl = buildFullUrl(targetUrl, request);

            HttpEntity<byte[]> requestEntity;
            if (StringUtils.isNotBlank(body)) {
                requestEntity = new HttpEntity<>(body.getBytes(), headers);
            } else {
                requestEntity = new HttpEntity<>(headers);
            }
            return restTemplate.exchange(
                    fullUrl,
                    HttpMethod.valueOf(request.getMethod()),
                    requestEntity,
                    byte[].class
            );
        } catch (Exception e) {
            String err = JsonUtils.extractMessage(e.getMessage());
            return ResponseEntity.status(500).body(err.getBytes());
        }
    }

    private String buildFullUrl(String baseUrl, HttpServletRequest request) {
        String path = request.getRequestURI();
        String query = request.getQueryString();

        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        String fullUrl = baseUrl + path;
        if (StringUtils.isNotBlank(query)) {
            fullUrl += "?" + query;
        }
        return fullUrl;
    }
}
