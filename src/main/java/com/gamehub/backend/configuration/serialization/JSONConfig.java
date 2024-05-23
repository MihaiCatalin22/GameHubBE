package com.gamehub.backend.configuration.serialization;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

@Configuration
public class JSONConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.simpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        };
    }

    @Bean
    public ObjectMapper getObjectMapper() {

        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
