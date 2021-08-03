package org.nhindirect.dns;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(exclude= {SecurityAutoConfiguration.class, 
		ReactiveSecurityAutoConfiguration.class})
@EnableFeignClients({"org.nhind.config.rest.feign"})
@ComponentScan({"org.nhindirect.config", "org.nhindirect.dns"})
@EnableR2dbcRepositories("org.nhindirect.config.repository")
@Import(HttpMessageConvertersAutoConfiguration.class)
public class TestApplication
{
    public static void main(String[] args) 
    {
        new SpringApplicationBuilder(TestApplication.class).web(WebApplicationType.REACTIVE).run(args);
    }  
}
