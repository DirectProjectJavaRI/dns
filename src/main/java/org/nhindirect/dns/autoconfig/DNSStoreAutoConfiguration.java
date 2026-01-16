package org.nhindirect.dns.autoconfig;

import org.nhind.config.rest.CertPolicyService;
import org.nhind.config.rest.CertificateService;
import org.nhind.config.rest.DNSService;
import org.nhindirect.dns.DNSStore;
import org.nhindirect.dns.RESTServiceDNSStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class DNSStoreAutoConfiguration
{
	@Value("${direct.dns.certPolicyName:}")
	protected String cerlPolicyName;
	
	@Bean
	@ConditionalOnMissingBean
	DNSStore dnsStore(DNSService dnsService, CertificateService certService, CertPolicyService certPolicyService)
	{
		final RESTServiceDNSStore dnsStore = 
				new RESTServiceDNSStore(dnsService, certService, certPolicyService, cerlPolicyName);
		
		return dnsStore;
	}
}
