package org.nhindirect.dns.springconfig;

import org.nhindirect.dns.DNSServerSettings;
import org.nhindirect.dns.service.DNSServerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DNSServerConfig
{
	@Value("${direct.dns.binding.port:53}")
	protected int port;
	
	@Value("${direct.dns.binding.address:0.0.0.0}")
	protected String bindAddress;
	
	@Bean
	@ConditionalOnMissingBean
	public DNSServerSettings dnsServerSettings()
	{
		final DNSServerSettings settings = new DNSServerSettings();
		settings.setBindAddress(bindAddress);
		settings.setPort(port);
		
		return settings;
	}
	
	@Bean(destroyMethod="stopService")
	@ConditionalOnMissingBean
	public DNSServerService dnsServerService()
	{
		return new DNSServerService();
	}
}
