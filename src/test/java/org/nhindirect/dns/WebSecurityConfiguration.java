package org.nhindirect.dns;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class WebSecurityConfiguration
{	
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http)
	{

        http.authorizeExchange(exchanges -> {
        	exchanges.anyExchange().permitAll();
        });
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
	}
    
}

