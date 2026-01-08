package org.nhindirect.dns;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


@SpringBootApplication
public class TestApplication
{
    public static void main(String[] args) 
    {
        new SpringApplicationBuilder(TestApplication.class).web(WebApplicationType.REACTIVE).run(args);
    }  
}
