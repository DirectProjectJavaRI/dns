package org.nhindirect.dns;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.config.model.CertPolicy;
import org.nhindirect.config.model.Certificate;
import org.nhindirect.config.model.DNSRecord;
import org.nhindirect.dns.util.BaseTestPlan;
import org.nhindirect.dns.util.DNSRecordUtil;
import org.nhindirect.dns.util.IPUtils;
import org.nhindirect.policy.PolicyLexicon;
import org.xbill.DNS.CERTRecord;
import org.xbill.DNS.Cache;
import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

public class DNSServer_Function_Test extends SpringBaseTest
{
	static final String KEY_ENC_POLICY = "(X509.TBS.EXTENSION.KeyUsage & 32) > 0";
	

	
	private static class Query
	{
		public String name;
		public int type;

		public Query(String name, int type)
		{
			this.name = name;
			this.type = type;
		}
	}

	private Certificate xCertToCert(X509Certificate cert) throws Exception
	{
		Certificate retVal = new Certificate();
		retVal.setOwner(CryptoExtensions.getSubjectAddress(cert));
		retVal.setData(cert.getEncoded());

		return retVal;
	}

	abstract class TestPlan extends BaseTestPlan
	{

		@Override
		protected void setupMocks() throws Exception
		{

			addRecords();

			dnsServer.startServer();
		}

		@Override
		protected void tearDownMocks() throws Exception
		{
			if (dnsServer != null)
				dnsServer.stopService();
		}

		@Override
		protected void performInner() throws Exception
		{
			ExtendedResolver resolver = new ExtendedResolver(IPUtils.getDNSLocalIps());
			resolver.setTimeout(300);

			resolver.setTCP(true);
			resolver.setPort(settings.getPort());

			Collection<Record> retrievedRecord = new ArrayList<Record>();

			Collection<Query> queries = getTestQueries();
			for (Query query : queries)
			{
				Lookup lu = new Lookup(new Name(query.name), query.type);
				Cache ch = Lookup.getDefaultCache(DClass.IN);
				ch.clearCache();
				lu.setResolver(resolver);

				Record[] retRecords = lu.run();
				if (retRecords != null && retRecords.length > 0)
					retrievedRecord.addAll(Arrays.asList(retRecords));
			}

			doAssertions(retrievedRecord);
		}

		protected abstract void addRecords() throws Exception;

		protected abstract Collection<Query> getTestQueries() throws Exception;

		protected abstract void doAssertions(Collection<Record> records) throws Exception;

	}

	/*
	public void testLoadTest() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DnsRecord> recs = new ArrayList<DnsRecord>();
				DnsRecord rec = DNSRecordUtil.createARecord("example.domain.com", "127.0.0.1");
				recs.add(rec);
	
				rec = DNSRecordUtil.createARecord("example2.domain.com", "127.0.0.1");
				recs.add(rec);
	
				proxy.addDNS(recs.toArray(new DnsRecord[recs.size()]));
	
			}
	
			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("example2.domain.com", Type.A));
	
				return queries;
			}
			
			@Override
			protected void performInner() throws Exception
			{
				
				final Runnable resolveRunner = new Runnable()
				{
					public void run()
					{
						try
						{
							ExtendedResolver resolver = new ExtendedResolver(new String[]{"127.0.0.1"});
							resolver.setTimeout(300);
			
							resolver.setTCP(true);
							resolver.setPort(1053);
			
							Collection<Record> retrievedRecord = new ArrayList<Record>();
			
							Collection<Query> queries = getTestQueries();
							for (Query query : queries)
							{
								Lookup lu = new Lookup(new Name(query.name), query.type);
								Cache ch = Lookup.getDefaultCache(DClass.IN);
								ch.clearCache();
								lu.setResolver(resolver);
			
								Record[] retRecords = lu.run();
								if (retRecords != null && retRecords.length > 0)
									retrievedRecord.addAll(Arrays.asList(retRecords));
							}
						}
						catch (Exception e)
						{
							
						}
					}
				};
				
				for (int i = 0; i < 300; ++i)
				{
					final Thread thr = new Thread(resolveRunner);
					thr.start();
				}
				
				Thread.sleep(100000000);
				//doAssertions(retrievedRecord);
			}
			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(1, records.size());
				assertEquals("example2.domain.com.", records.iterator().next().getName().toString());
			}
		}.perform();		
	}
	*/
	
	@Test
	public void testQueryARecord_AssertRecordsRetrieved_NoSOA() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				final ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createARecord("example.domain.com", "127.0.0.1");
				recs.add(rec);

				rec = DNSRecordUtil.createARecord("example2.domain.com", "127.0.0.1");
				recs.add(rec);

				for (DNSRecord addRec : recs)
					dnsService.addDNSRecord(addRec);
			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("example2.domain.com", Type.A));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(1, records.size());
				assertEquals("example2.domain.com.", records.iterator().next().getName().toString());
			}
		}.perform();
	}

	@Test
	public void testQueryARecord_AssertRecordsRetrieved_SOARecord() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createARecord("example.domain.com", "127.0.0.1");
				recs.add(rec);

				rec = DNSRecordUtil.createARecord("example2.domain.com", "127.0.0.1");
				recs.add(rec);

				rec = DNSRecordUtil.createARecord("sub2.example2.domain.com", "127.0.0.1");
				recs.add(rec);

				rec = DNSRecordUtil.createSOARecord("domain.com", "nsserver.domain.com","master.domain.com");
				recs.add(rec);

				for (DNSRecord addRec : recs)
					dnsService.addDNSRecord(addRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("sub2.example2.domain.com", Type.A));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(1, records.size());
				assertEquals("sub2.example2.domain.com.", records.iterator().next().getName().toString());
			}
		}.perform();
	}

/*
	public void testQueryARecord_noRecordInDNSServer_assertDelegation() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DnsRecord> recs = new ArrayList<DnsRecord>();
				DnsRecord rec = DNSRecordUtil.createARecord("example.domain.com", "127.0.0.1");
				recs.add(rec);

				rec = DNSRecordUtil.createNSRecord("sub.example.domain.com", "127.0.0.3");
				recs.add(rec);

				proxy.addDNS(recs.toArray(new DnsRecord[recs.size()]));

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("sub.example.domain.com", Type.A));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(1, records.size());
				Record rec = records.iterator().next();
				assertEquals("sub.example.domain.com.", rec.getName().toString());
				assertEquals(Type.NS, rec.getType());
			}
		}.perform();
	}
*/
	
	@Test
	public void testQueryARecordByAny_AssertRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createARecord("example.domain.com", "127.0.0.1");
				recs.add(rec);

				rec = DNSRecordUtil.createARecord("example2.domain.com", "127.0.0.1");
				recs.add(rec);

				for (DNSRecord addRec : recs)
					dnsService.addDNSRecord(addRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("example2.domain.com", Type.ANY));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(1, records.size());
				assertEquals("example2.domain.com.", records.iterator().next().getName().toString());
			}
		}.perform();
	}

	@Test
	public void testQueryMutliARecords_AssertRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createARecord("example.domain.com", "127.0.0.1");
				recs.add(rec);

				rec = DNSRecordUtil.createARecord("example.domain.com", "127.0.0.2");
				recs.add(rec);

				rec = DNSRecordUtil.createSOARecord("domain.com", "nsserver.domain.com","master.domain.com");
				recs.add(rec);

				for (DNSRecord addRec : recs)
					dnsService.addDNSRecord(addRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("example.domain.com", Type.A));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(2, records.size());
				assertEquals("example.domain.com.", records.iterator().next().getName().toString());
			}
		}.perform();
	}

	@Test
	public void testQueryARecords_AssertNoRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{

			}

			protected Collection<Query> getTestQueries() throws Exception
			{

				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("example.domain.com", Type.A));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(0, records.size());
			}
		}.perform();
	}

	@Test
	public void testAnyQueryType_multipleTypesInRecord_AssertRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createARecord("example.domain.com", "127.0.0.1");
				recs.add(rec);

				rec = DNSRecordUtil.createARecord("example.domain.com", "127.0.0.2");
				recs.add(rec);

				rec = DNSRecordUtil.createMXRecord("example.domain.com", "domain.com", 1);
				recs.add(rec);
				
				for (DNSRecord addRec : recs)
					dnsService.addDNSRecord(addRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("example.domain.com", Type.ANY));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(3, records.size());
				assertEquals("example.domain.com.", records.iterator().next().getName().toString());
			}
		}.perform();		
	}
	
	@Test
	public void testQueryCERTRecords_AssertRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				// add some CERT records
				ArrayList<Certificate> recs = new ArrayList<Certificate>();

				X509Certificate cert = DNSRecordUtil.loadCertificate("bob.der");
				Certificate addCert = xCertToCert(cert);
				recs.add(addCert);

				cert = DNSRecordUtil.loadCertificate("gm2552.der");
				addCert = xCertToCert(cert);
				recs.add(addCert);

				cert = DNSRecordUtil.loadCertificate("ryan.der");
				addCert = xCertToCert(cert);
				recs.add(addCert);

				for (Certificate addCertRec : recs)
					certService.addCertificate(addCertRec);


				ArrayList<DNSRecord> soaRecs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createSOARecord("securehealthemail.com", "nsserver.securehealthemail.com","master.securehealthemail.com");
				soaRecs.add(rec);

				for (DNSRecord addRec : soaRecs)
					dnsService.addDNSRecord(addRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("gm2552.securehealthemail.com", Type.CERT));
				queries.add(new Query("ryan.securehealthemail.com", Type.ANY));
				queries.add(new Query("bob.somewhere.com", Type.A));

				return queries;

			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(2, records.size());

				boolean foundGreg = false;
				boolean foundRyan = false;
				for (Record record : records)
				{
					assertTrue(record instanceof CERTRecord);

					X509Certificate cert = (X509Certificate)DNSRecordUtil.parseRecord((CERTRecord)record);
					assertNotNull(cert);

					if (CryptoExtensions.getSubjectAddress(cert).equals("gm2552@securehealthemail.com"))
						foundGreg = true;
					else if (CryptoExtensions.getSubjectAddress(cert).equals("ryan@securehealthemail.com"))
						foundRyan = true;
				}

				assertTrue(foundGreg);
				assertTrue(foundRyan);
			}
		}.perform();
	}

	@Test
	public void testQueryCERTRecords_policyExists_AssertRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			
			@Override
			public void tearDownMocks() throws Exception
			{
				dnsStore.setCertPolicyName("");
			}
			
			@Override
			protected void setupMocks() throws Exception
			{				
				// create the key encypherment policy
				final CertPolicy policy = new CertPolicy();
				policy.setLexicon(PolicyLexicon.SIMPLE_TEXT_V1);
				policy.setPolicyName("ValidPolicy");
				policy.setPolicyData(KEY_ENC_POLICY.getBytes());
				
				certPolService.addPolicy(policy);

				addRecords();
				
				dnsServer.startServer();
				
				dnsStore.setCertPolicyName("ValidPolicy");
			}
			
			protected void addRecords() throws Exception
			{
				// add some CERT records
				ArrayList<Certificate> recs = new ArrayList<Certificate>();

				X509Certificate cert = DNSRecordUtil.loadCertificate("bob.der");
				Certificate addCert = xCertToCert(cert);
				recs.add(addCert);
				

				cert = DNSRecordUtil.loadCertificate("umesh.der");
				addCert = xCertToCert(cert);
				recs.add(addCert);

				for (Certificate addRec : recs)
					certService.addCertificate(addRec);


				ArrayList<DNSRecord> soaRecs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createSOARecord("securehealthemail.com", "nsserver.securehealthemail.com","master.securehealthemail.com");
				soaRecs.add(rec);

				rec = DNSRecordUtil.createSOARecord("nhind.hsgincubator.com", "nsserver.nhind.hsgincubator.com","master.nhind.hsgincubator.com");
				soaRecs.add(rec);

				for (DNSRecord addSoaRec : soaRecs)
					dnsService.addDNSRecord(addSoaRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("bob.nhind.hsgincubator.com", Type.CERT));
				queries.add(new Query("umesh.securehealthemail.com", Type.CERT));
				
				return queries;

			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(1, records.size());

				final Record record = records.iterator().next();
				assertTrue(record instanceof CERTRecord);

				X509Certificate cert = (X509Certificate)DNSRecordUtil.parseRecord((CERTRecord)record);
				assertNotNull(cert);

				CryptoExtensions.getSubjectAddress(cert).equals("bob@nhind.hsgincubator.com");
			}
		}.perform();
	}
	
	@Test
	public void testQueryCERTRecords_AssertNoRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("gm2552.securehealthemail.com", Type.CERT));
				queries.add(new Query("ryan.securehealthemail.com", Type.ANY));
				queries.add(new Query("bob.somewhere.com", Type.A));

				return queries;

			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(0, records.size());

			}
		}.perform();
	}
	
	@Test
	public void testQueryMXRecord_AssertRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createMXRecord("domain.com", "example.domain.com", 1);
				recs.add(rec);

				rec = DNSRecordUtil.createMXRecord("domain.com", "example2.domain.com", 2);
				recs.add(rec);

				rec = DNSRecordUtil.createMXRecord("domain2.com", "example.domain2.com", 1);
				recs.add(rec);

				for (DNSRecord addRec : recs)
					dnsService.addDNSRecord(addRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("domain.com", Type.MX));
				queries.add(new Query("domain.com", Type.A));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(2, records.size());
				assertEquals("domain.com.", records.iterator().next().getName().toString());
			}
		}.perform();
	}

	@Test
	public void testQueryMXRecordByA_AssertNoRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createMXRecord("domain.com", "example.domain.com", 1);
				recs.add(rec);

				rec = DNSRecordUtil.createMXRecord("domain.com", "example2.domain.com", 2);
				recs.add(rec);

				rec = DNSRecordUtil.createMXRecord("domain2.com", "example.domain2.com", 1);
				recs.add(rec);

				for (DNSRecord addRec : recs)
					dnsService.addDNSRecord(addRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("domain.com", Type.A));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(0, records.size());
			}
		}.perform();
	}

	@Test
	public void testQueryNSRecord_AssertRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createNSRecord("domain.com", "ns.domain.com");
				recs.add(rec);

				rec = DNSRecordUtil.createNSRecord("domain.com", "ns2.domain.com");
				recs.add(rec);

				rec = DNSRecordUtil.createNSRecord("domain2.com", "ns.domain2.com");
				recs.add(rec);

				for (DNSRecord addRec : recs)
					dnsService.addDNSRecord(addRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("domain.com", Type.NS));
				queries.add(new Query("domain.com", Type.A));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(2, records.size());
				assertEquals("domain.com.", records.iterator().next().getName().toString());

				for (Record rec : records)
				{
					assertEquals(Type.NS, rec.getType());
				}
			}
		}.perform();
	}

	@Test
	public void testQueryNSRecordByA_AssertNoRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createNSRecord("domain.com", "ns.domain.com");
				recs.add(rec);

				rec = DNSRecordUtil.createNSRecord("domain.com", "ns2.domain.com");
				recs.add(rec);

				rec = DNSRecordUtil.createNSRecord("domain2.com", "ns.domain2.com");
				recs.add(rec);

				for (DNSRecord addRec : recs)
					dnsService.addDNSRecord(addRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("domain.com", Type.A));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(0, records.size());
			}
		}.perform();
	}

	@Test
	public void testQueryCNAMERecord_AssertRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createCNAMERecord("domainserver.com", "domain.com");
				recs.add(rec);

				rec = DNSRecordUtil.createCNAMERecord("domainserver2.com", "domain.com");
				recs.add(rec);

				rec = DNSRecordUtil.createCNAMERecord("domain2server.com", "domain2.com");
				recs.add(rec);

				for (DNSRecord addRec : recs)
					dnsService.addDNSRecord(addRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("domainserver.com", Type.CNAME));
				queries.add(new Query("domainserver2.com", Type.CNAME));
				queries.add(new Query("domain.com", Type.A));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(2, records.size());
				assertEquals("domainserver.com.", records.iterator().next().getName().toString());

				for (Record rec : records)
				{
					assertEquals(Type.CNAME, rec.getType());
				}
			}
		}.perform();
	}

	@Test
	public void testQueryCNAMERecordByA_AssertNoRecordsRetrieved() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createCNAMERecord("domainserver.com", "domain.com");
				recs.add(rec);

				rec = DNSRecordUtil.createCNAMERecord("domainserver2.com", "domain.com");
				recs.add(rec);

				rec = DNSRecordUtil.createCNAMERecord("domain2server.com", "domain2.com");
				recs.add(rec);

				for (DNSRecord addRec : recs)
					dnsService.addDNSRecord(addRec);

			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("domain.com", Type.A));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(0, records.size());
			}
		}.perform();
	}

	@Test
	public void testQueryUnsupportedQueryType() throws Exception
	{
		new TestPlan()
		{
			protected void addRecords() throws Exception
			{
				ArrayList<DNSRecord> recs = new ArrayList<>();
				DNSRecord rec = DNSRecordUtil.createCNAMERecord("domainserver.com", "domain.com");
				recs.add(rec);

				dnsService.addDNSRecord(rec);
			}

			protected Collection<Query> getTestQueries() throws Exception
			{
				Collection<Query> queries = new ArrayList<Query>();
				queries.add(new Query("domain.com", Type.AAAA));

				return queries;
			}

			protected void doAssertions(Collection<Record> records) throws Exception
			{
				assertNotNull(records);
				assertEquals(0, records.size());
			}
		}.perform();
	}
}
