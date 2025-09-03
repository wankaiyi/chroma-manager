package com.wky.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author wky
 * @date 2025/09/03
 */
@Configuration
public class RestTemplateConfig {

    private final OkHttpClient okHttpClient;

    public RestTemplateConfig(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Bean
    public RestTemplate restTemplate() {
        OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
        return new RestTemplate(factory);
    }

    @Bean(name = "fastRestTemplate")
    public RestTemplate fastRestTemplate() {
        OkHttpClient fastClient = okHttpClient.newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(fastClient);
        return new RestTemplate(factory);
    }

    @Bean(name = "longTimeoutRestTemplate")
    public RestTemplate longTimeoutRestTemplate() {
        OkHttpClient longTimeoutClient = okHttpClient.newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(longTimeoutClient);
        return new RestTemplate(factory);
    }
}