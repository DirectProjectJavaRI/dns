package org.nhindirect.dns;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class WebSecurityConfiguration
{
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http)
	{
		http.csrf().disable();

	    return http.build(); 
	}
}

