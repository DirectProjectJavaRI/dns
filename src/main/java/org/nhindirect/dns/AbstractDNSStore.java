package org.nhindirect.dns;

import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.nhindirect.policy.PolicyExpression;
import org.nhindirect.policy.PolicyFilter;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;
import org.xbill.DNS.InvalidTypeException;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Opcode;
import org.xbill.DNS.RRset;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDNSStore implements DNSStore
{
	
	protected static final String DNS_CERT_POLICY_NAME_VAR = "org.nhindirect.dns.CertPolicyName";
	
	protected static final String DEFAULT_JCE_PROVIDER_STRING = "BC";
	protected static final String JCE_PROVIDER_STRING_SYS_PARAM = "org.nhindirect.dns.JCEProviderName";	
	
	protected Map<String, Record> soaRecords = null;
	
	protected PolicyFilter polFilter = null;
	protected PolicyExpression polExpression = null;
	
	static
	{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	
	/**
	 * Gets the configured JCE crypto provider string for crypto operations.  This is configured using the
	 * -Dorg.nhindirect.dns.JCEProviderName JVM parameters.  If the parameter is not set or is empty,
	 * then the default string "BC" (BouncyCastle provider) is returned.  By default the agent installs the BouncyCastle provider.
	 * @return The name of the JCE provider string.
	 */
	public static String getJCEProviderName()
	{
		String retVal = System.getProperty(JCE_PROVIDER_STRING_SYS_PARAM);
		
		if (retVal == null || retVal.isEmpty())
			retVal = DEFAULT_JCE_PROVIDER_STRING;
		
		return retVal;
	}
	
	/**
	 * Overrides the configured JCE crypto provider string.  If the name is empty or null, the default string "BC" (BouncyCastle provider)
	 * is used.
	 * @param name The name of the JCE provider.
	 */
	public static void setJCEProviderName(String name)
	{
		if (name == null || name.isEmpty())
			System.setProperty(JCE_PROVIDER_STRING_SYS_PARAM, DEFAULT_JCE_PROVIDER_STRING);
		else
			System.setProperty(JCE_PROVIDER_STRING_SYS_PARAM, name);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Message get(Message request) throws DNSException
	{
		log.trace("get(Message) Entered");
		/* for testing time out cases
		try
		{
			Thread.sleep(1000000);
		}
		catch (Exception e)
		{

		}
	    */
		if (request == null)
			throw new DNSException(DNSError.newError(Rcode.FORMERR));
		
		Header header = request.getHeader();
		if (header.getFlag(Flags.QR) || header.getRcode() != Rcode.NOERROR)
			throw new DNSException(DNSError.newError(Rcode.FORMERR));

		if (header.getOpcode() != Opcode.QUERY)
			throw new DNSException(DNSError.newError(Rcode.NOTIMP));	
		
        Record queryRecord = request.getQuestion();
        
        if (queryRecord == null || queryRecord.getDClass() != DClass.IN)
        {
        	throw new DNSException(DNSError.newError(Rcode.NOTIMP));
        }

        Name name = queryRecord.getName();
    	int type = queryRecord.getType();
    	String typeString = null;
    	try {
    		typeString = Type.string(type);
    	} catch(InvalidTypeException e) {
    	}
        
    	if (log.isDebugEnabled())
    	{
    		StringBuilder builder = new StringBuilder("Received Query Request:");
    		builder.append("\r\n\tName: " + name.toString());
    		builder.append("\r\n\tType: " + (typeString == null ? type : typeString));
    		builder.append("\r\n\tDClass: " + queryRecord.getDClass());
    		log.debug(builder.toString());
    	}

    	log.info("Process record for DNS request type " + (typeString == null ? type : typeString) + " and name " + name.toString());
    	
    	Collection<Record> lookupRecords= null;
        switch (type)
        {
        	case Type.A:
        	case Type.MX:
        	case Type.SOA:
        	case Type.SRV:
        	case Type.NS:
        	case Type.CNAME: 
        	case Type.TXT:
        	case Type.CAA:
        	{
        		try
        		{
        			final RRset set = processGenericRecordRequest(name.toString(), type);
        			
        			if (set != null)
        			{
	        			lookupRecords = set.rrs();
        			}
        			
        		}
        		catch (Exception e)
        		{
        			throw new DNSException(DNSError.newError(Rcode.SERVFAIL), "DNS service proxy call failed: " + e.getMessage(), e);
        		}
        		break;
        	}
        	case Type.CERT:
        	{
    			final RRset set = processCERTRecordRequest(name.toString());
    			
    			if (set != null)
    			{
	    			lookupRecords = set.rrs();
    			}
    			
        		break;
        	}
        	case Type.ANY:
        	{
        		
        		Collection<Record> genRecs = processGenericANYRecordRequest(name.toString());
        		RRset certRecs = processCERTRecordRequest(name.toString());

        		if (genRecs != null || certRecs != null)
        		{
        			lookupRecords = new ArrayList<Record>();
        			if (genRecs != null)
        				lookupRecords.addAll(genRecs);
        			
        			if (certRecs != null)
        			{
        				lookupRecords = certRecs.rrs();
        			}
        		}
 
        		break;
        	}
        	default:
        	{
        		log.debug("Query Type " + (typeString == null ? type : typeString) + " not implemented");
        		throw new DNSException(DNSError.newError(Rcode.NOTIMP), "Query Type " + (typeString == null ? type : typeString) + " not implemented"); 
        	}        	
        }
     
        
        if (lookupRecords == null || lookupRecords.size() == 0)
        {
        	log.debug("No records found.");
        	return null;
        }
        	
        final Message response = new Message(request.getHeader().getID());
        response.getHeader().setFlag(Flags.QR);
    	if (request.getHeader().getFlag(Flags.RD))
    		response.getHeader().setFlag(Flags.RD);
    	response.addRecord(queryRecord, Section.QUESTION);
    	
    	
		final Iterator<Record> iter = lookupRecords.iterator();
		while (iter.hasNext())
			response.addRecord(iter.next(), Section.ANSWER);
    	
    	// we are authoritative only
    	response.getHeader().setFlag(Flags.AA);
    	// look for an SOA record
    	final Record soaRecord = checkForSoaRecord(name.toString());
    	if (soaRecord != null)
    		response.addRecord(soaRecord, Section.AUTHORITY);		
		
		log.trace("get(Message) Exit");
		
    	return response;
	}
	
	/**
	 * Processes all DNS requests except CERT records.
	 * @param name The record name.
	 * @param type The record type.
	 * @return Returns a set of record responses to the request.
	 * @throws DNSException
	 */
	protected abstract RRset processGenericRecordRequest(String name, int type) throws DNSException;
	
	/**
	 * Processes all DNS CERT requests.
	 * @param name The record name.  In many cases this a email address.
	 * @return Returns a set of record responses to the request.
	 * @throws DNSException
	 */
	protected abstract RRset processCERTRecordRequest(String name) throws DNSException;
	
	protected abstract Collection<Record> processGenericANYRecordRequest(String name) throws DNSException;
	
	protected abstract Record checkForSoaRecord(String questionName);
	
	protected boolean isCertCompliantWithPolicy(X509Certificate cert)
	{
		// if no policy has been set, then always return true
		if (this.polFilter == null)
			return true;
		
		try
		{
			return this.polFilter.isCompliant(cert, this.polExpression);
		}
		catch (Exception e)
		{
			log.warn("Error testing certificate for policy compliance.  Default to compliant.", e);
			return true;
		}
	}	
}
