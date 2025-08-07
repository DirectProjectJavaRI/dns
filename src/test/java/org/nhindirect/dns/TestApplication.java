package org.nhindirect.dns;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude= {SecurityAutoConfiguration.class, 
		ReactiveSecurityAutoConfiguration.class})
@ComponentScan({"org.nhindirect.config"})
@Import(HttpMessageConvertersAutoConfiguration.class)
public class TestApplication
{
    public static void main(String[] args) 
    {
        new SpringApplicationBuilder(TestApplication.class).web(WebApplicationType.REACTIVE).run(args);
    }  
}
