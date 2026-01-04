package com.example.config.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Thymeleaf template configuration for notification templates.
 */
@Configuration
@RequiredArgsConstructor
public class NotificationTemplateConfig {

    private final NotificationProperties properties;

    /**
     * Template resolver for HTML templates (EMAIL, IN_APP)
     */
    @Bean(name = "notificationHtmlTemplateResolver")
    public SpringResourceTemplateResolver htmlTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix(properties.getTemplate().getBasePath());
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(properties.getTemplate().isCacheEnabled());
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1);
        resolver.setCheckExistence(true);
        return resolver;
    }

    /**
     * Template resolver for TEXT templates (SMS, WHATSAPP)
     */
    @Bean(name = "notificationTextTemplateResolver")
    public SpringResourceTemplateResolver textTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix(properties.getTemplate().getBasePath());
        resolver.setSuffix(".txt");
        resolver.setTemplateMode(TemplateMode.TEXT);
        resolver.setCacheable(properties.getTemplate().isCacheEnabled());
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(2);
        resolver.setCheckExistence(true);
        return resolver;
    }

    /**
     * Template resolver for JSON templates (PUSH)
     */
    @Bean(name = "notificationJsonTemplateResolver")
    public SpringResourceTemplateResolver jsonTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix(properties.getTemplate().getBasePath());
        resolver.setSuffix(".json");
        resolver.setTemplateMode(TemplateMode.TEXT);
        resolver.setCacheable(properties.getTemplate().isCacheEnabled());
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(3);
        resolver.setCheckExistence(true);
        return resolver;
    }

    /**
     * Template engine for notification templates.
     * Configured separately from the main web template engine.
     */
    @Bean(name = "notificationTemplateEngine")
    public SpringTemplateEngine notificationTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(htmlTemplateResolver());
        engine.addTemplateResolver(textTemplateResolver());
        engine.addTemplateResolver(jsonTemplateResolver());
        engine.setEnableSpringELCompiler(true);
        return engine;
    }
}
