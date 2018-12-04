package org.nhindirect.dns.springconfig;

import org.nhind.config.rest.CertPolicyService;
import org.nhind.config.rest.CertificateService;
import org.nhind.config.rest.DNSService;
import org.nhind.config.rest.feign.CertificateClient;
import org.nhind.config.rest.feign.CertificatePolicyClient;
import org.nhind.config.rest.feign.DNSClient;
import org.nhind.config.rest.impl.DefaultCertPolicyService;
import org.nhind.config.rest.impl.DefaultCertificateService;
import org.nhind.config.rest.impl.DefaultDNSService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients({"org.nhind.config.rest.feign"})
public class ConfigServiceClientConfig
{
	@Bean
	@ConditionalOnMissingBean
	public CertificateService certificateService(CertificateClient certClient)
	{
		return new DefaultCertificateService(certClient);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public CertPolicyService certPolicyService(CertificatePolicyClient polClient)
	{
		return new DefaultCertPolicyService(polClient);
	}	
	
	@Bean
	@ConditionalOnMissingBean
	public DNSService dnsService(DNSClient dnsClient)
	{
		return new DefaultDNSService(dnsClient);
	}		
}
