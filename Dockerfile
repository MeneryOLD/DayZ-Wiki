FROM eclipse-temurin:17-jdk-alpine AS builder
RUN apk add --no-cache mysql-client
WORKDIR /dayzwiki
COPY /target/dayz-wiki-0.0.1-SNAPSHOT.jar /dayzwiki/dayzwiki.jar
FROM nginx:1.18.0
RUN apt-get update && apt-get install -y brotli
COPY --from=builder /dayzwiki/dayzwiki.jar /usr/share/nginx/html/
RUN echo 'load_module /usr/share/nginx/modules/ngx_http_brotli_filter_module.so;' >> /etc/nginx/nginx.conf
RUN echo 'load_module /usr/share/nginx/modules/ngx_http_brotli_static_module.so;' >> /etc/nginx/nginx.conf
EXPOSE 8080 80
CMD nginx && java -jar /usr/share/nginx/html/dayzwiki.jar