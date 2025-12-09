package com.file.registry.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public ObjectMapper jsonObjectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule());
  }

  @Bean
  public XmlMapper xmlMapper() {
    return new XmlMapper();
  }
}
