package org.nhindirect.dns;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;

import org.nhindirect.config.model.CertPolicy;
import org.nhindirect.dns.util.DNSRecordUtil;
import org.nhindirect.policy.PolicyExpression;
import org.nhindirect.policy.PolicyFilter;
import org.nhindirect.policy.PolicyLexicon;

public class RESTServiceDNSStore_isCertCompliantWithPolicyTest extends SpringBaseTest
{
	static final String KEY_ENC_POLICY = "(X509.TBS.EXTENSION.KeyUsage & 32) > 0";
	
	@Test
	public void testisCertCompliantWithPolicy_noPolicyConfigured_assertCompliant() throws Exception
	{
		
		assertNull(dnsStore.polExpression);
		assertNull(dnsStore.polFilter);
		
		X509Certificate cert = DNSRecordUtil.loadCertificate("bob.der");
		
		assertTrue(dnsStore.isCertCompliantWithPolicy(cert));
	}
	
	@Test
	public void testisCertCompliantWithPolicy_policyConfigured_compliantCert_assertCompliant() throws Exception
	{
		try
		{
			final CertPolicy policy = new CertPolicy();
			policy.setLexicon(PolicyLexicon.SIMPLE_TEXT_V1);
			policy.setPolicyName("ValidPolicy");
			policy.setPolicyData(KEY_ENC_POLICY.getBytes());
			
			certPolService.addPolicy(policy);
			
			dnsStore.setCertPolicyName("ValidPolicy");
			
			assertNotNull(dnsStore.polExpression);
			assertNotNull(dnsStore.polFilter);
			
			X509Certificate cert = DNSRecordUtil.loadCertificate("bob.der");
			
			assertTrue(dnsStore.isCertCompliantWithPolicy(cert));
		}
		finally
		{
			dnsStore.setCertPolicyName("");
		}
	}
	
	@Test
	public void testisCertCompliantWithPolicy_policyConfigured_nonCompliantCert_assertNonCompliant() throws Exception
	{
		try
		{
			final CertPolicy policy = new CertPolicy();
			policy.setLexicon(PolicyLexicon.SIMPLE_TEXT_V1);
			policy.setPolicyName("ValidPolicy");
			policy.setPolicyData(KEY_ENC_POLICY.getBytes());
			
			certPolService.addPolicy(policy);
			
			dnsStore.setCertPolicyName("ValidPolicy");
			
			assertNotNull(dnsStore.polExpression);
			assertNotNull(dnsStore.polFilter);
			
			X509Certificate cert = DNSRecordUtil.loadCertificate("umesh.der");
			
			assertFalse(dnsStore.isCertCompliantWithPolicy(cert));
		}
		finally
		{
			dnsStore.setCertPolicyName("");
		}
	}
	
	@Test
	public void testisCertCompliantWithPolicy_exceptionInFilter_assertCompliant() throws Exception
	{
		final PolicyFilter filt = mock(PolicyFilter.class);
		
		doThrow(new RuntimeException("Just Passing Through")).when(filt).isCompliant((X509Certificate)any(), (PolicyExpression)any());
		
		dnsStore.polFilter = filt;
		
		X509Certificate cert = DNSRecordUtil.loadCertificate("umesh.der");
		
		assertTrue(dnsStore.isCertCompliantWithPolicy(cert));

	}
}
