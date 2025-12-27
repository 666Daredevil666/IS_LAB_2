package ru.itmo.is.musicband.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableJms
@EnableScheduling
public class JmsConfig {
}

