package io.falconFlow.configuration;

import com.github.jknack.handlebars.Handlebars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlebarsConfig {

    @Bean
    public Handlebars newHandlebars(){
        return  new Handlebars();
    }

}
