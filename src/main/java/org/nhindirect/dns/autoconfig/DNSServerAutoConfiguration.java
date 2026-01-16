package org.nhindirect.dns.autoconfig;

import org.nhindirect.dns.DNSServerSettings;
import org.nhindirect.dns.service.DNSServerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class DNSServerAutoConfiguration
{
	@Value("${direct.dns.binding.port:53}")
	protected int port;
	
	@Value("${direct.dns.binding.address:0.0.0.0}")
	protected String bindAddress;
	
	@Value("${direct.dns.binding.maxReconnectAttempts:10}")
	protected int maxReconnectAttempts;
	
	@Bean
	@ConditionalOnMissingBean
	DNSServerSettings dnsServerSettings()
	{
		final DNSServerSettings settings = new DNSServerSettings();
		settings.setBindAddress(bindAddress);
		settings.setPort(port);
		settings.setMaxReconnectAttempts(maxReconnectAttempts);
		
		return settings;
	}
	
	@Bean(destroyMethod="stopService")
	@ConditionalOnMissingBean
	DNSServerService dnsServerService()
	{
		return new DNSServerService();
	}
}
