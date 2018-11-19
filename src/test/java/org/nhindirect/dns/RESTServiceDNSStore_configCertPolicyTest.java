package org.nhindirect.dns;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.nhindirect.config.model.CertPolicy;
import org.nhindirect.policy.PolicyLexicon;

public class RESTServiceDNSStore_configCertPolicyTest extends SpringBaseTest
{
	static final String VALID_POLICY = "(X509.TBS.EXTENSION.KeyUsage & 32) > 0";
	static final String INVALID_VALID_POLICY = "(X509.TBS.EXTENSION.KeyUsage4fds & | 32) > 0";
	
	@Test
	public void testConfigCertPolicy_policyDoesNotExists_assertNoPolicyConfiged() throws Exception
	{

		assertNull(dnsStore.polExpression);
		assertNull(dnsStore.polFilter);

	}
	
	@Test
	public void testConfigCertPolicy_invalidPolicy_assertNoPolicyConfiged() throws Exception
	{

		try
		{
			final CertPolicy policy = new CertPolicy();
			policy.setLexicon(PolicyLexicon.SIMPLE_TEXT_V1);
			policy.setPolicyName("InvalidPolicy");
			policy.setPolicyData(INVALID_VALID_POLICY.getBytes());
			
			certPolService.addPolicy(policy);
			
			dnsStore.setCertPolicyName("InvalidPolicy");
			
			assertNull(dnsStore.polExpression);
			assertNull(dnsStore.polFilter);
		}
		finally
		{
			dnsStore.setCertPolicyName("");
		}
	}

	@Test
	public void testConfigCertPolicy_validPolicy_assertPolicyConfiged() throws Exception
	{

		try
		{
			final CertPolicy policy = new CertPolicy();
			policy.setLexicon(PolicyLexicon.SIMPLE_TEXT_V1);
			policy.setPolicyName("ValidPolicy");
			policy.setPolicyData(VALID_POLICY.getBytes());
			
			certPolService.addPolicy(policy);
			
			dnsStore.setCertPolicyName("ValidPolicy");
			
			assertNotNull(dnsStore.polExpression);
			assertNotNull(dnsStore.polFilter);
		}
		finally
		{
			dnsStore.setCertPolicyName("");
		}
	}	
	
	
}
