package org.nhindirect.dns.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.commons.io.FileUtils;
import org.nhindirect.config.store.DNSRecord;
import org.nhindirect.config.store.util.DNSRecordUtils;
import org.xbill.DNS.DClass;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.CERTRecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Name;

public class DNSRecordUtil 
{
	private static final String certBasePath = "src/test/resources/certs/"; 
	
	private static org.nhindirect.config.model.DNSRecord toDnsRecord(DNSRecord rec)
	{
		org.nhindirect.config.model.DNSRecord retVal = new org.nhindirect.config.model.DNSRecord();
		
		retVal.setData(rec.getData());
		retVal.setDclass(rec.getDclass());
		retVal.setName(rec.getName());
		retVal.setTtl(rec.getTtl());
		retVal.setType(rec.getType());
		
		return retVal;
	}

	
	public static X509Certificate loadCertificate(String certFileName) throws Exception
	{
		File fl = new File(certBasePath + certFileName);
		
		InputStream str = new ByteArrayInputStream(FileUtils.readFileToByteArray(fl));
		
		X509Certificate retVal = (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(str);
		
		str.close();
		
		return retVal;
	}
	
	
	public static org.nhindirect.config.model.DNSRecord createARecord(String name, String ip) throws Exception
	{
		DNSRecord rec = DNSRecordUtils.createARecord(name, 86400L, ip);
		
		return toDnsRecord(rec);
	}

	public static org.nhindirect.config.model.DNSRecord createCERTRecord(String name, X509Certificate cert) throws Exception
	{
		DNSRecord rec = DNSRecordUtils.createX509CERTRecord(name, 86400L, cert);
		
		return toDnsRecord(rec);
	}
	
	public static org.nhindirect.config.model.DNSRecord createMXRecord(String name, String target, int priority) throws Exception
	{
		DNSRecord rec = DNSRecordUtils.createMXRecord(name, target, 86400L, priority);
		
		return toDnsRecord(rec);
	}	
	
	public static org.nhindirect.config.model.DNSRecord createSOARecord(String name, String nameServer, String hostmaster) throws Exception
	{
		DNSRecord rec = DNSRecordUtils.createSOARecord(name, 3600L, nameServer, hostmaster, 1, 3600L, 600L, 604800L, 3600L);
		
		return toDnsRecord(rec);
	}		
	
	public static org.nhindirect.config.model.DNSRecord createNSRecord(String name, String target) throws Exception
	{
		
		if (!name.endsWith("."))
			name = name + ".";
		
		if (!target.endsWith("."))
			target = target + ".";
		
		NSRecord rec = new NSRecord(Name.fromString(name), DClass.IN, 86400L, Name.fromString(target));
		
		return toDnsRecord(DNSRecord.fromWire(rec.toWireCanonical()));
		
	}	
	
	public static org.nhindirect.config.model.DNSRecord createCNAMERecord(String name, String target) throws Exception
	{
		
		if (!name.endsWith("."))
			name = name + ".";
		
		if (!target.endsWith("."))
			target = target + ".";
		
		CNAMERecord rec = new CNAMERecord(Name.fromString(name), DClass.IN, 86400L, Name.fromString(target));
		
		return toDnsRecord(DNSRecord.fromWire(rec.toWireCanonical()));
		
	}	
	
	public static Certificate parseRecord(CERTRecord r) 
	{
		int type = r.getCertType();
		byte[] data = r.getCert();

		try 
		{
			switch (type) 
			{
				case 1 :
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
					ByteArrayInputStream bs = new ByteArrayInputStream(data);
					Certificate cert = cf.generateCertificate(bs);
					return cert;
				default :
					return null;
			}
		} 
		catch (CertificateException var6) 
		{

			return null;
		}
	}	
}
