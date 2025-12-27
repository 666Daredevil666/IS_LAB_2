package ru.itmo.is.musicband.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter f = new CommonsRequestLoggingFilter();
        f.setIncludeClientInfo(true);
        f.setIncludeHeaders(true);
        f.setIncludePayload(true);
        f.setIncludeQueryString(true);
        f.setMaxPayloadLength(2048);
        return f;
    }
}

