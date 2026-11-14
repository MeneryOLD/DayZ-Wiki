package com.dayzwiki.portal.config;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.concurrent.TimeUnit;

/**
 * Browser caching for static files. Without this, Spring Security marks every response "no-store",
 * so the browser downloaded the CSS, scripts and images again on every page.
 *
 * CSS and JS get a content hash in their URL (/css/app-3f2a….css, via @{...} links in the templates),
 * so they can be cached for a year and still update the moment a new version is deployed.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] locations;

    public WebConfig(WebProperties webProperties) {
        this.locations = webProperties.getResources().getStaticLocations();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        for (String folder : new String[]{"css", "js"}) {
            registry.addResourceHandler("/" + folder + "/**")
                    .addResourceLocations(sub(folder + "/"))
                    .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                    .resourceChain(true)
                    .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
        }

        registry.addResourceHandler("/img/**")
                .addResourceLocations(sub("img/"))
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());

        registry.addResourceHandler("/data/**")
                .addResourceLocations(sub("data/"))
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic());
    }

    /** Each static location (classpath:/static/, or a folder in dev) + the given sub-folders. */
    private String[] sub(String... folders) {
        String[] out = new String[locations.length * folders.length];
        int i = 0;
        for (String loc : locations) {
            String base = loc.endsWith("/") ? loc : loc + "/";
            for (String f : folders) {
                out[i++] = base + f;
            }
        }
        return out;
    }

    /** Rewrites @{/css/app.css} in templates to the hashed file name. */
    @Bean
    public FilterRegistrationBean<ResourceUrlEncodingFilter> resourceUrlEncodingFilter() {
        return new FilterRegistrationBean<>(new ResourceUrlEncodingFilter());
    }

    /** Avatars and covers: answer "304 Not Modified" when the picture has not changed. */
    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> profileImageEtags() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> bean = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        bean.addUrlPatterns("/profile/get-avatar", "/profile/get-banner");
        return bean;
    }
}
