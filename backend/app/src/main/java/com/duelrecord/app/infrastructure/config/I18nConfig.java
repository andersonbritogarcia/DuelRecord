package com.duelrecord.app.infrastructure.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;

import java.nio.charset.StandardCharsets;

@Configuration
public class I18nConfig {

    private static final String MESSAGES_BASENAME = "i18n/messages";

    /**
     * Bean name must be {@code localeResolver}: Spring MVC's {@code DispatcherServlet} looks it
     * up by this exact name to resolve the locale for each request and populate
     * {@link org.springframework.context.i18n.LocaleContextHolder}.
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new AppLocaleResolver();
    }

    @Bean
    public MessageSource messageSource() {
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames(MESSAGES_BASENAME);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setUseCodeAsDefaultMessage(false);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    /**
     * Wires Bean Validation (e.g. {@code @NotNull}, {@code @Size}) to the same
     * {@link MessageSource}, so validation constraint messages can be localized using the
     * application's message bundles.
     */
    @Bean
    public LocalValidatorFactoryBean getValidator(MessageSource messageSource) {
        var validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource);
        return validatorFactoryBean;
    }
}
