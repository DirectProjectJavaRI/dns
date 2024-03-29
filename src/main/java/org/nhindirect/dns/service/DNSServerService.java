/* 
Copyright (c) 2010, NHIN Direct Project
All rights reserved.

Authors:
   Greg Meyer      gm2552@cerner.com
 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
in the documentation and/or other materials provided with the distribution.  Neither the name of the The NHIN Direct Project (nhindirect.org). 
nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
THE POSSIBILITY OF SUCH DAMAGE.
*/


package org.nhindirect.dns.service;

import org.nhindirect.dns.DNSException;
import org.nhindirect.dns.DNSServer;
import org.nhindirect.dns.DNSServerSettings;
import org.nhindirect.dns.DNSStore;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

/**
 * Service wrapper that instantiates and configures the DNS server.
 * @author Greg Meyer
 *
 * @since 1.0
 */
@Slf4j
public class DNSServerService 
{
	@Autowired
	protected DNSStore dnsStore;
	
	@Autowired 
	protected DNSServerSettings settings;
	
	protected DNSServer server;

	public DNSServerService()
	{
		
	}
	
	public void startServer() throws DNSException
	{
		log.info("Creating the DNSServer");
		
		server = new DNSServer(dnsStore, settings);
		
		log.info("DNS Server created.  Starting server.");
		server.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() 
		{
		    public void run() 
		    { 
		    	try
		    	{
		    		log.info("Shutdown hook detected.  Intiate server shutdown.");
		    		stopService();
		    	}
		    	catch (DNSException e) {/* no-op */}
		    	
		    }
		});
	}
	

	/**
	 * Stops and shutdown the service.
	 * @throws DNSException
	 * 
	 * @since 1.0
	 */
	public synchronized void stopService() throws DNSException
	{
		if (server != null)
		{
			log.info("Shutting down DNS server.");
			server.stop();
		}

	}
}
