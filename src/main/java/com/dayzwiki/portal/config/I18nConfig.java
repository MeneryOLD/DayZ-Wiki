package com.dayzwiki.portal.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

/**
 * Site language: English (default) or Russian.
 * The choice is kept in the "dz-lang" cookie; "?lang=ru" or "?lang=en" switches it.
 * Without the cookie, the browser's Accept-Language header decides.
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    public static final String COOKIE = "dz-lang";

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver(COOKIE);
        resolver.setCookieMaxAge(Duration.ofDays(365));
        resolver.setCookiePath("/");
        resolver.setDefaultLocaleFunction(request -> {
            Locale browser = request.getLocale();
            return browser != null && "ru".equals(browser.getLanguage()) ? Locale.forLanguageTag("ru") : Locale.ENGLISH;
        });
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        // Never fall back to the server's own locale: unknown languages get English.
        source.setFallbackToSystemLocale(false);
        return source;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        interceptor.setIgnoreInvalidLocale(true);
        registry.addInterceptor(interceptor);
    }
}
