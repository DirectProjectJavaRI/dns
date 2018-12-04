package org.nhindirect.dns.springconfig;

import org.nhind.config.rest.CertPolicyService;
import org.nhind.config.rest.CertificateService;
import org.nhind.config.rest.DNSService;
import org.nhindirect.dns.DNSStore;
import org.nhindirect.dns.RESTServiceDNSStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DNSStoreConfig
{
	@Value("${direct.dns.certPolicyName:}")
	protected String cerlPolicyName;
	
	@Autowired
	protected DNSService dnsService;
	
	@Autowired
	protected CertificateService certService;
	
	@Autowired
	protected CertPolicyService certPolicyService;
	
	@Bean
	@ConditionalOnMissingBean
	public DNSStore dnsStore()
	{
		final RESTServiceDNSStore dnsStore = 
				new RESTServiceDNSStore(dnsService, certService, certPolicyService, cerlPolicyName);
		
		return dnsStore;
	}
}
